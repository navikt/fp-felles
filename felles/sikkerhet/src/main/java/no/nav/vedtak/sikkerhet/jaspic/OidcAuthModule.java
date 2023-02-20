package no.nav.vedtak.sikkerhet.jaspic;

import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.TokenCallback;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.JwtUtil;

/**
 * Stjålet mye fra https://github.com/omnifaces/omnisecurity
 *
 * Klassen er og må være thread-safe da den vil brukes til å kalle på subjects
 * samtidig. Se {@link ServerAuthModule}. Skal derfor ikke inneholde felter som
 * holder Subject, token eller andre parametere som kommer med en request eller
 * session.
 */
public class OidcAuthModule implements ServerAuthModule {

    private static final String OIDC_LOGIN_CONFIG = "OIDC";
    private static final Logger LOG = LoggerFactory.getLogger(OidcAuthModule.class);

    private static final Class<?>[] SUPPORTED_MESSAGE_TYPES = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    // Key in the MessageInfo Map that when present AND set to true indicated a
    // protected resource is being accessed.
    // When the resource is not protected, GlassFish omits the key altogether.
    // WebSphere does insert the key and sets
    // it to false.
    private static final String IS_MANDATORY = "javax.security.auth.message.MessagePolicy.isMandatory";

    private final TokenLocator tokenLocator;
    private final Configuration loginConfiguration;

    private CallbackHandler containerCallbackHandler;

    private final List<DelegatedProtectedResource> delegatedProtectedList;

    public OidcAuthModule() {
        this.tokenLocator = new TokenLocator();
        this.loginConfiguration = new LoginContextConfiguration();

        this.delegatedProtectedList = new ArrayList<>();
        ServiceLoader.load(DelegatedProtectedResource.class, OidcAuthModule.class.getClassLoader()).forEach(delegatedProtectedList::add);
    }

    /**
     * used for unit-testing
     */
    OidcAuthModule(TokenLocator tokenLocator, Configuration loginConfiguration,
                   DelegatedProtectedResource delegatedProtectedResource) {
        this.tokenLocator = tokenLocator;
        this.loginConfiguration = loginConfiguration;
        this.delegatedProtectedList = delegatedProtectedResource == null ? List.of() : List.of(delegatedProtectedResource);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException {
        this.containerCallbackHandler = handler;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class[] getSupportedMessageTypes() {
        return SUPPORTED_MESSAGE_TYPES;
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        HttpServletRequest originalRequest = (HttpServletRequest) messageInfo.getRequestMessage();
        setCallAndConsumerId(originalRequest);
        validateCleanSubjecthandler();
        AuthStatus authStatus;

        if (isProtected(messageInfo)) {
            authStatus = handleProtectedResource(clientSubject, originalRequest);
        } else {
            authStatus = handleUnprotectedResource(clientSubject);
        }

        if (FAILURE.equals(authStatus)) {
            // Vurder om trengs whitelisting av oppslag mot isAlive/isReady/metrics gitt oppførsel i prod - app må registrere unprotected
            // Sjekk av whitelist/unprotected mot HttpServletRequest / getPathInfo må foregå før valg av protected/unprotected
            HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Resource is protected, but id token is missing or invalid.");
            } catch (IOException e) {
                throw new TekniskException("F-396795", "Klarte ikke å sende respons", e);
            }
            return SEND_CONTINUE; // TODO - skal man returnere SEND_FAILURE, FAILURE? SEND_CONTINUE virker mest relevant for redirect to login
        }

        if (SUCCESS.equals(authStatus)) {
            messageInfo.setRequestMessage(new StatelessHttpServletRequest((HttpServletRequest) messageInfo.getRequestMessage()));
        }
        return authStatus;
    }

    protected void validateCleanSubjecthandler() {
        final Subject subject = SubjectHandler.getSubjectHandler().getSubject();
        if (subject != null  && SubjectHandler.getSubjectHandler() instanceof ThreadLocalSubjectHandler tlsh) {
            final Set<String> credidentialClasses = new HashSet<>();
            for (Object publicCredential : subject.getPublicCredentials()) {
                credidentialClasses.add(publicCredential.getClass().getName());
            }
            LOG.error(
                "Denne SKAL rapporteres som en bug hvis den dukker opp. Tråden inneholdt allerede et Subject med følgende principals {} og PublicCredentials klasser {}. Sletter det før autentisering fortsetter.",
                subject.getPrincipals(), credidentialClasses);
            tlsh.setSubject(null);
        }
    }

    public void setCallAndConsumerId(HttpServletRequest request) {
        String callId = Optional.ofNullable(request.getHeader(MDCOperations.HTTP_HEADER_CALL_ID)) // NOSONAR Akseptertet headere
            .orElseGet(() -> request.getHeader(MDCOperations.HTTP_HEADER_ALT_CALL_ID));
        if (callId != null) {
            MDCOperations.putCallId(callId);
        } else {
            MDCOperations.putCallId();
        }

        String consumerId = request.getHeader(MDCOperations.HTTP_HEADER_CONSUMER_ID); // NOSONAR Akseptertet headere
        if (consumerId != null) {
            MDCOperations.putConsumerId(consumerId);
        }
    }

    protected AuthStatus oidcLogin(Subject clientSubject, HttpServletRequest request) {
        // Get token
        var oidcToken = tokenLocator.getToken(request);
        if (oidcToken.isEmpty()) {
            return FAILURE;
        }
        var claims = oidcToken.map(TokenString::token).map(JwtUtil::getClaims);
        var configuration = claims.map(JwtUtil::getIssuer).flatMap(ConfigProvider::getOpenIDConfiguration);
        if (configuration.isEmpty()) {
            return FAILURE;
        }

        var expiresAt = claims.map(JwtUtil::getExpirationTime).orElseGet(() -> Instant.now().plusSeconds(300));
        var token = new OpenIDToken(configuration.get().type(), OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE, oidcToken.get(), null, expiresAt.toEpochMilli());

        // Setup login context
        LoginContext loginContext = createLoginContext(clientSubject, token);

        // Do login
        try {
            loginContext.login();
        } catch (LoginException e) {
            return FAILURE;
        }

        // Flytt nærmere tokenvalidering når JA-SPI + JAAS saneres
        var sluttbruker = SubjectHandler.getSluttBruker(clientSubject);
        KontekstHolder.setKontekst(RequestKontekst.forRequest(sluttbruker.getName(), sluttbruker.getShortUid(), sluttbruker.getIdentType(), token));

        // Handle result
        return handleValidatedToken(clientSubject, SubjectHandler.getUid(clientSubject));
    }

    private LoginContext createLoginContext(Subject clientSubject, OpenIDToken token) {
        class TokenCallbackHandler implements CallbackHandler {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof TokenCallback tc) {
                        tc.setToken(token);
                    } else {
                        // Should never happen
                        throw new UnsupportedCallbackException(callback, TokenCallback.class + " is the only supported Callback");
                    }
                }
            }
        }

        CallbackHandler callbackHandler = new TokenCallbackHandler();
        try {
            return new LoginContext(OIDC_LOGIN_CONFIG, clientSubject, callbackHandler, loginConfiguration);
        } catch (LoginException le) {
            throw new TekniskException("F-651753", String.format("Kunne ikke finne konfigurasjonen for %s", OIDC_LOGIN_CONFIG), le);
        }
    }

    protected AuthStatus handleProtectedResource(Subject clientSubject, HttpServletRequest originalRequest) {
        var delegatedAuthStatus = delegatedProtectedList.stream()
                .map(d -> d.handleProtectedResource(originalRequest, clientSubject, containerCallbackHandler))
                .flatMap(Optional::stream)
                .findFirst();

        if (delegatedAuthStatus.isEmpty()) {
            return oidcLogin(clientSubject, originalRequest);
        } else {
            // delegert autentisering
            return delegatedAuthStatus.get();
        }
    }

    protected AuthStatus handleUnprotectedResource(Subject clientSubject) {
        KontekstHolder.setKontekst(BasisKontekst.ikkeAutentisertRequest(MDCOperations.getConsumerId()));
        return notifyContainerAboutLogin(clientSubject, null);
    }

    protected AuthStatus handleValidatedToken(Subject clientSubject, String username) {
        AuthStatus authStatus = notifyContainerAboutLogin(clientSubject, username);
        // HACK (u139158): Must be taken from clientSubject @see
        // OidcAuthModule#notifyContainerAboutLogin(Subject, String)
        MDCOperations.putUserId(SubjectHandler.getUid(clientSubject));
        if (MDCOperations.getConsumerId() == null) {
            MDCOperations.putConsumerId(SubjectHandler.getConsumerId(clientSubject));
        }
        return authStatus;
    }

    /**
     * Asks the container to register the given username.
     * <p>
     * <p>
     * Note that after this call returned, the authenticated identity will not be
     * immediately active. This will only take place (should not errors occur) after
     * the {@link ServerAuthContext} or {@link ServerAuthModule} in which this call
     * takes place return control back to the runtime.
     * <p>
     * <p>
     * As a convenience this method returns SUCCESS, so this method can be used in
     * one fluent return statement from an auth module.
     *
     * @param username the user name that will become the caller principal
     * @return {@link AuthStatus#SUCCESS}
     */
    private AuthStatus notifyContainerAboutLogin(Subject clientSubject, String username) {
        try {
            containerCallbackHandler.handle(new Callback[] { new CallerPrincipalCallback(clientSubject, username) });
        } catch (IOException | UnsupportedCallbackException e) {
            // Should not happen
            throw new IllegalStateException(e);
        }
        return SUCCESS;
    }

    private boolean isProtected(MessageInfo messageInfo) {
        return Boolean.parseBoolean((String) messageInfo.getMap().get(IS_MANDATORY));
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        if (KontekstHolder.harKontekst()) {
            KontekstHolder.fjernKontekst();
        } else {
            LOG.info("FPFELLES KONTEKST fant ikke kontekst som forventet i secureResponse");
        }
        MDC.clear();
        return SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        if (KontekstHolder.harKontekst()) {
            LOG.info("FPFELLES KONTEKST hadde kontekst ved cleanSubject");
            KontekstHolder.fjernKontekst();
        }
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

}
