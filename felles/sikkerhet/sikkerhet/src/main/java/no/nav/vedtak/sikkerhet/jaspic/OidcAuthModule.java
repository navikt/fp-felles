package no.nav.vedtak.sikkerhet.jaspic;

import static javax.security.auth.message.AuthStatus.FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_CONTINUE;
import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames.OIDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.loginmodule.LoginModuleFeil;
import no.nav.vedtak.sikkerhet.oidc.IdTokenProvider;

/**
 * Stjålet mye fra https://github.com/omnifaces/omnisecurity
 *
 * Klassen er og må være thread-safe da den vil brukes til å kalle på subjects samtidig. Se {@link ServerAuthModule}.  Skal derfor ikke inneholde felter som holder Subject, token eller andre parametere som kommer med en request eller session.
 */
public class OidcAuthModule implements ServerAuthModule {
    private static final Logger LOG = LoggerFactory.getLogger(OidcAuthModule.class);

    private static final Class<?>[] SUPPORTED_MESSAGE_TYPES = new Class[]{HttpServletRequest.class, HttpServletResponse.class};
    // Key in the MessageInfo Map that when present AND set to true indicated a protected resource is being accessed.
    // When the resource is not protected, GlassFish omits the key altogether. WebSphere does insert the key and sets
    // it to false.
    private static final String IS_MANDATORY = "javax.security.auth.message.MessagePolicy.isMandatory";

    private final IdTokenProvider tokenProvider;
    private final TokenLocator tokenLocator;
    private final Configuration loginConfiguration;
    private final WSS4JProtectedServlet wsServlet;
    private final Set<String> wsServletPaths;

    private CallbackHandler containerCallbackHandler;

    public OidcAuthModule() {
        tokenProvider = new IdTokenProvider();
        tokenLocator = new TokenLocator();
        loginConfiguration = findLoginContextConfiguration();
        wsServlet = findWSS4JProtectedServlet();
        wsServletPaths = findWsServletPaths(wsServlet);
    }

    /**
     * used for unit-testing
     */
    OidcAuthModule(IdTokenProvider tokenProvider, TokenLocator tokenLocator, Configuration loginConfiguration, WSS4JProtectedServlet wsServlet) {
        this.tokenProvider = tokenProvider;
        this.tokenLocator = tokenLocator;
        this.loginConfiguration = loginConfiguration;
        this.wsServlet = wsServlet;
        wsServletPaths = findWsServletPaths(wsServlet);
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
            if (usingSamlForAuthentication(originalRequest)) {
                authStatus = verifyProtectedLater(originalRequest, clientSubject);
            } else {
                authStatus = oidcLogin(messageInfo, clientSubject, originalRequest);
            }
        } else {
            authStatus = handleUnprotectedResource(clientSubject);
        }

        if (FAILURE.equals(authStatus)) {
            authStatus = responseUnAuthorized(messageInfo);
        }

        if (SUCCESS.equals(authStatus)) {
            ensureStatelessApplication(messageInfo);
        }
        return authStatus;
    }

    private void validateCleanSubjecthandler() {
        final Subject subject = SubjectHandler.getSubjectHandler().getSubject();
        if (subject != null) {
            if (SubjectHandler.getSubjectHandler() instanceof ThreadLocalSubjectHandler) {
                final Set<String> credidentialClasses = new HashSet<>();
                for (Object publicCredential : subject.getPublicCredentials()) {
                    credidentialClasses.add(publicCredential.getClass().getName());
                }
                JaspicFeil.FACTORY.eksisterendeSubject(new HashSet<>(subject.getPrincipals()), credidentialClasses).log(LOG);
                ((ThreadLocalSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(null);
            }
        }
    }

    public void setCallAndConsumerId(HttpServletRequest request) {
        String callId = request.getHeader(MDCOperations.HTTP_HEADER_CALL_ID); //NOSONAR Akseptertet headere
        if (callId != null) {
            MDCOperations.putCallId(callId);
        } else {
            MDCOperations.putCallId();
        }

        String consumerId = request.getHeader(MDCOperations.HTTP_HEADER_CONSUMER_ID); //NOSONAR Akseptertet headere
        if (consumerId != null) {
            MDCOperations.putConsumerId(consumerId);
        }
    }

    private AuthStatus verifyProtectedLater(HttpServletRequest request, Subject clientSubject) {
        if (wsServlet.isProtectedWithAction(request.getPathInfo(), WSHandlerConstants.SAML_TOKEN_SIGNED)) {
            return handleProtectedLaterResource(clientSubject);
        }
        return FAILURE;
    }

    private boolean usingSamlForAuthentication(HttpServletRequest request) {
        return !isGET(request) && request.getServletPath()!=null && wsServletPaths.contains(request.getServletPath());
    }

    /**
     * JAX-WS only supports SOAP over POST
     *
     * @see org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor#isGET(SoapMessage)
     */
    private final boolean isGET(HttpServletRequest request) {
        return "GET".equals(request.getMethod());
    }

    public AuthStatus oidcLogin(MessageInfo messageInfo, Subject clientSubject, HttpServletRequest request) {
        // Get token
        Optional<OidcTokenHolder> oidcToken = tokenLocator.getToken(request);
        Optional<String> refreshToken = tokenLocator.getRefreshToken(request);

        OidcTokenHolder tokenHolder;
        if (oidcToken.isPresent()) {
            tokenHolder = oidcToken.get();
        } else {
            return FAILURE;
        }

        // Setup login context
        LoginContext loginContext = createLoginContext(OIDC, clientSubject, tokenHolder);

        // Do login
        try {
            try {
                loginContext.login();
            } catch (CredentialExpiredException cee) { // NOSONAR
                Optional<OidcTokenHolder> tokenRefreshed = idTokenRefreshed(tokenHolder, refreshToken);
                if (tokenRefreshed.isPresent()) {
                    OidcTokenHolder newToken = tokenRefreshed.get();
                    LoginContext loginContextRefresh = createLoginContext(OIDC, clientSubject, newToken);
                    loginContextRefresh.login();
                    registerUpdatedTokenAtUserAgent(messageInfo, newToken);
                } else {
                    return FAILURE;
                }
            }
        } catch (LoginException le) {
            return FAILURE;
        }

        // Handle result
        return handleValidatedToken(clientSubject, SubjectHandler.getUid(clientSubject));
    }

    private Optional<OidcTokenHolder> idTokenRefreshed(OidcTokenHolder originalToken, Optional<String> refreshToken) {
        if (refreshToken.isPresent()) {
            Optional<OidcTokenHolder> nyttToken = tokenProvider.getToken(originalToken, refreshToken.get());
            return nyttToken;
        }
        return Optional.empty();
    }

    private LoginContext createLoginContext(LoginConfigNames loginConfigName, Subject clientSubject, OidcTokenHolder tokenHolder) {
        class TokenCallbackHandler implements CallbackHandler {
            @Override
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof TokenCallback) {
                        ((TokenCallback) callback).setToken(tokenHolder);
                    } else {
                        // Should never happen
                        throw new UnsupportedCallbackException(callback, TokenCallback.class + " is the only supported Callback");
                    }
                }
            }
        }

        CallbackHandler callbackHandler = new TokenCallbackHandler();
        try {
            return new LoginContext(loginConfigName.name(), clientSubject, callbackHandler, loginConfiguration);
        } catch (LoginException le) {
            throw FeilFactory.create(LoginModuleFeil.class).kunneIkkeFinneLoginmodulen(loginConfigName.name(), le).toException();
        }
    }

    /**
     * Wrapps the request in a object that throws an {@link IllegalArgumentException} when invoking getSession og getSession(true)
     */
    private void ensureStatelessApplication(MessageInfo messageInfo) {
        messageInfo.setRequestMessage(new StatelessHttpServletRequest((HttpServletRequest) messageInfo.getRequestMessage()));
    }

    private void registerUpdatedTokenAtUserAgent(MessageInfo messageInfo, OidcTokenHolder updatedIdToken) {
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        response.addCookie(lagCookie(ID_TOKEN_COOKIE_NAME, updatedIdToken.getToken(), "/", ServerInfo.instance().getCookieDomain()));
    }

    private Cookie lagCookie(String name, String value, String path, String domain) {
        Cookie cookie = new Cookie(name, value); //NOSONAR findsecbugs:HTTP_RESPONSE_SPLITTING, Fikset i JBoss EAP 7.0.2, ref https://access.redhat.com/security/cve/CVE-2016-4993
        cookie.setSecure(ServerInfo.instance().isUsingTLS());
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        return cookie;
    }

    private AuthStatus handleUnprotectedResource(Subject clientSubject) {
        return notifyContainerAboutLogin(clientSubject, null);
    }

    private AuthStatus handleProtectedLaterResource(Subject clientSubject) {
        return notifyContainerAboutLogin(clientSubject, "SAML");
    }

    private AuthStatus handleValidatedToken(Subject clientSubject, String username) {
        AuthStatus authStatus = notifyContainerAboutLogin(clientSubject, username);
        //HACK (u139158): Must be taken from clientSubject @see OidcAuthModule#notifyContainerAboutLogin(Subject, String)
        MDCOperations.putUserId(SubjectHandler.getUid(clientSubject));
        if(MDCOperations.getConsumerId() == null) {
            MDCOperations.putConsumerId(SubjectHandler.getConsumerId(clientSubject));
        }
        return authStatus;
    }

    private AuthStatus responseUnAuthorized(MessageInfo messageInfo) {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        String acceptHeader = request.getHeader("Accept");
        String authorizationHeader = request.getHeader("Authorization"); //NOSONAR Akseptertet headere
        try {
            if ((acceptHeader != null && acceptHeader.contains("application/json"))
                    || (authorizationHeader != null && authorizationHeader.startsWith("Bearer "))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Resource is protected, but id token is missing or invalid.");
            } else {
                AuthorizationRequestBuilder builder = new AuthorizationRequestBuilder();
                //TODO (u139158): CSRF attack protection. See RFC-6749 section 10.12 (the state-cookie containing redirectURL shold be encrypted to avoid tampering)
                response.addCookie(lagCookie(builder.getStateIndex(), encode(getOriginalUrl(request)), ServerInfo.instance().getRelativeCallbackUrl(), null));
                response.sendRedirect(builder.buildRedirectString());
            }
        } catch (IOException e) {
            throw OidcAuthModuleFeil.FACTORY.klarteIkkeSendeRespons(e).toException();
        }
        return SEND_CONTINUE;
    }

    private String encode(String redirectLocation) throws UnsupportedEncodingException {
        return URLEncoder.encode(redirectLocation, StandardCharsets.UTF_8.name());
    }

    protected String getOriginalUrl(HttpServletRequest req) {
        String requestUrl = req.getRequestURL().toString();
        // Scheme i request vi mottar er ikke det samme som det sluttbrukeren ser når TLS termineres underveis i kallkjeden
        // https -> http -> appserver
        if(ServerInfo.instance().isUsingTLS() && !requestUrl.toLowerCase().startsWith("https")){
            requestUrl = requestUrl.replaceFirst("http","https");
        }
        return req.getQueryString() == null
                ? requestUrl
                : requestUrl + "?" + req.getQueryString();
    }

    /**
     * Asks the container to register the given username.
     * <p>
     * <p>
     * Note that after this call returned, the authenticated identity will not be immediately active. This
     * will only take place (should not errors occur) after the {@link ServerAuthContext} or {@link ServerAuthModule}
     * in which this call takes place return control back to the runtime.
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
            containerCallbackHandler.handle(new Callback[]{new CallerPrincipalCallback(clientSubject, username)});
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

    private LoginContextConfiguration findLoginContextConfiguration() {
        // No need for bean.destroy(instance) since it's ApplicationScoped
        return CDI.current().select(LoginContextConfiguration.class).get();
    }

    private static Set<String> findWsServletPaths(WSS4JProtectedServlet wsServlet) {
        return wsServlet == null ? Collections.emptySet() : Set.copyOf(wsServlet.getUrlPatterns());
    }

    private WSS4JProtectedServlet findWSS4JProtectedServlet() {
        Set<Bean<?>> beans = CDI.current().getBeanManager().getBeans(WSS4JProtectedServlet.class);
        if (beans.isEmpty()) {
            //hvis applikasjonen ikke tilbyr webservice, har den heller ikke WSS4JProtectedServlet
            return null;
        }
        // No need for bean.destroy(instance) since it's ApplicationScoped
        return CDI.current().select(WSS4JProtectedServlet.class).get();
    }

}
