package no.nav.vedtak.sikkerhet.jaspic;

import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.jwt.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.isso.oidc.OpenAmTokenProvider;
import no.nav.vedtak.isso.ressurs.IssoAuthorizationRequestBuilder;
import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.OidcLogin;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

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

    private OpenAmTokenProvider openAmTokenProvider;
    private final TokenLocator tokenLocator;
    private final Configuration loginConfiguration;

    private CallbackHandler containerCallbackHandler;

    private final List<DelegatedProtectedResource> delegatedProtectedList;

    public OidcAuthModule() {
        this.openAmTokenProvider = new OpenAmTokenProvider();
        this.tokenLocator = new TokenLocator();
        this.loginConfiguration = new LoginContextConfiguration();

        this.delegatedProtectedList = new ArrayList<>();
        ServiceLoader.load(DelegatedProtectedResource.class, OidcAuthModule.class.getClassLoader()).forEach(delegatedProtectedList::add);
    }

    /**
     * used for unit-testing
     */
    OidcAuthModule(OpenAmTokenProvider tokenProvider, TokenLocator tokenLocator, Configuration loginConfiguration,
                   DelegatedProtectedResource delegatedProtectedResource) {
        this.openAmTokenProvider = tokenProvider;
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
            authStatus = handleProtectedResource(messageInfo, clientSubject, originalRequest);
        } else {
            authStatus = handleUnprotectedResource(clientSubject);
        }

        if (FAILURE.equals(authStatus)) {
            // Eventuell redirect til ekstern Login for å få id_token
            authStatus = responseUnAuthorized(messageInfo);
        }

        if (SUCCESS.equals(authStatus)) {
            ensureStatelessApplication(messageInfo);
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

    protected AuthStatus oidcLogin(MessageInfo messageInfo, Subject clientSubject, HttpServletRequest request) {
        // Get token
        var oidcToken = tokenLocator.getToken(request);
        if (oidcToken.isEmpty()) {
            return FAILURE;
        }
        var refreshToken = tokenLocator.getRefreshToken(request);
        var claims = oidcToken.map(TokenString::token).map(JwtUtil::getClaims);
        var configuration = claims.map(JwtUtil::getIssuer).flatMap(ConfigProvider::getOpenIDConfiguration);
        if (configuration.isEmpty()) {
            return FAILURE;
        }
        if (OpenIDProvider.ISSO.equals(configuration.get().type())) {
            LOG.info("OPENAM incoming openam");
        }

        var expiresAt = claims.map(JwtUtil::getExpirationTime).orElseGet(() -> Instant.now().plusSeconds(300));
        var token = new OpenIDToken(configuration.get().type(), OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE, oidcToken.get(), null,
            refreshToken.orElseGet(() -> new TokenString(null)), expiresAt.toEpochMilli());

        var refreshed = refreshCookieTokenVedBehov(request, token, claims.orElse(null));
        refreshed.ifPresent(r -> registerUpdatedTokenAtUserAgent(messageInfo, r));
        token = refreshed.orElse(token);

        // Setup login context
        LoginContext loginContext = createLoginContext(clientSubject, token);

        // Do login
        try {
            loginContext.login();
        } catch (LoginException e) {
            return FAILURE;
        }

        // Handle result
        return handleValidatedToken(clientSubject, SubjectHandler.getUid(clientSubject));
    }

    private Optional<OpenIDToken> refreshCookieTokenVedBehov(HttpServletRequest request, OpenIDToken token, JwtClaims claims) {
        if (OpenIDProvider.ISSO.equals(token.provider()) && openAmTokenProvider.isOpenAmTokenSoonExpired(token) && tokenLocator.isTokenFromCookie(request)
            && Set.of(OidcLogin.LoginResult.SUCCESS, OidcLogin.LoginResult.ID_TOKEN_EXPIRED).contains(OidcLogin.validerToken(token).loginResult())) {
            LOG.info("OPENAM refresh token");
            return openAmTokenProvider.refreshOpenAmIdToken(token, Optional.ofNullable(claims).map(JwtUtil::getClientName).orElse(null));
        }
        return Optional.empty();
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

    /**
     * Wrapps the request in a object that throws an
     * {@link IllegalArgumentException} when invoking getSession og getSession(true)
     */
    private void ensureStatelessApplication(MessageInfo messageInfo) {
        messageInfo.setRequestMessage(new StatelessHttpServletRequest((HttpServletRequest) messageInfo.getRequestMessage()));
    }

    private void registerUpdatedTokenAtUserAgent(MessageInfo messageInfo, OpenIDToken updatedIdToken) {
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        response.addCookie(lagCookie(ID_TOKEN_COOKIE_NAME, updatedIdToken.token(), "/", ServerInfo.instance().getCookieDomain()));
    }

    private Cookie lagCookie(String name, String value, String path, String domain) {
        Cookie cookie = new Cookie(name, value); // NOSONAR findsecbugs:HTTP_RESPONSE_SPLITTING, Fikset i JBoss EAP 7.0.2, ref
                                                 // https://access.redhat.com/security/cve/CVE-2016-4993
        cookie.setSecure(ServerInfo.instance().isUsingTLS());
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        return cookie;
    }

    protected AuthStatus handleProtectedResource(MessageInfo messageInfo, Subject clientSubject, HttpServletRequest originalRequest) {
        var delegatedAuthStatus = delegatedProtectedList.stream()
                .map(d -> d.handleProtectedResource(originalRequest, clientSubject, containerCallbackHandler))
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);

        if (delegatedAuthStatus.isEmpty()) {
            return oidcLogin(messageInfo, clientSubject, originalRequest);
        } else {
            // delegert autentisering
            return delegatedAuthStatus.get();
        }
    }

    protected AuthStatus handleUnprotectedResource(Subject clientSubject) {
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

    /*
     * Dersom req med Bearer: 401, ellers redirect til OpenAm login
     */
    protected AuthStatus responseUnAuthorized(MessageInfo messageInfo) {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        String acceptHeader = request.getHeader("Accept");
        String authorizationHeader = request.getHeader("Authorization"); // NOSONAR Akseptertet headere
        try {
            if ((acceptHeader != null && acceptHeader.contains("application/json"))
                    || (authorizationHeader != null && authorizationHeader.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Resource is protected, but id token is missing or invalid.");
            } else {
                LOG.info("OPENAM redirect login pga tom header");
                IssoAuthorizationRequestBuilder builder = new IssoAuthorizationRequestBuilder();
                // TODO (u139158): CSRF attack protection. See RFC-6749 section 10.12 (the
                // state-cookie containing redirectURL shold be encrypted to avoid tampering)
                response.addCookie(
                        lagCookie(builder.getStateIndex(), encode(getOriginalUrl(request)), ServerInfo.instance().getRelativeCallbackUrl(), null));
                response.sendRedirect(builder.buildRedirectString());
            }
        } catch (IOException e) {
            throw new TekniskException("F-396795", "Klarte ikke å sende respons", e);
        }
        return SEND_CONTINUE;
    }

    private String encode(String redirectLocation) {
        return URLEncoder.encode(redirectLocation, StandardCharsets.UTF_8);
    }

    protected String getOriginalUrl(HttpServletRequest req) {
        String requestUrl = req.getRequestURL().toString();
        // Scheme i request vi mottar er ikke det samme som det sluttbrukeren ser når
        // TLS termineres underveis i kallkjeden
        // https -> http -> appserver
        if (ServerInfo.instance().isUsingTLS() && !requestUrl.toLowerCase().startsWith("https")) {
            requestUrl = requestUrl.replaceFirst("http", "https");
        }
        return req.getQueryString() == null
                ? requestUrl
                : requestUrl + "?" + req.getQueryString();
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
        MDC.clear();
        return SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

}
