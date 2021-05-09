package no.nav.vedtak.sikkerhet.loginmodule.oidc;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.ressurs.TokenCallback;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;
import no.nav.vedtak.sikkerhet.loginmodule.LoginModuleBase;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.OidcLogin;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider;

/**
 * <p>
 * LoginModule that will use an OIDC ID Token and add NAV Principals and
 * Credentials.
 * </p>
 * <p>
 * Depends on either the invoker or another LoginModule in the chain to actually
 * set the SecurityContext.
 * </p>
 */
public class OIDCLoginModule extends LoginModuleBase {

    private static final Logger logger = LoggerFactory.getLogger(OIDCLoginModule.class);
    private static final int AUTHENTICATION_LEVEL_INTERN_BRUKER = 4;

    // Set during initialize()
    private Subject subject;
    private CallbackHandler callbackHandler;

    // Set during login()
    private OidcTokenHolder ssoToken;
    private SluttBruker sluttBruker;

    // Set during commit()
    private ConsumerId consumerId;
    private AuthenticationLevelCredential authenticationLevelCredential;
    private OidcCredential oidcCredential;

    public OIDCLoginModule() {
        super(logger);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        logger.trace("Initialize loginmodule with {} and {}", subject, callbackHandler);
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        logger.trace("Enter login method");
        ssoToken = getSSOToken();

        String issuer = JwtUtil.getIssuser(ssoToken.token());
        var tokenValidator = OidcTokenValidatorProvider.instance().getValidator(issuer);
        logger.trace("Issuer er {}, validator er {}", issuer, tokenValidator);
        OidcLogin oidcLogin = new OidcLogin(Optional.of(ssoToken), tokenValidator);
        OidcLogin.LoginResult loginResult = oidcLogin.doLogin();
        if (loginResult == OidcLogin.LoginResult.SUCCESS) {
            sluttBruker = SluttBruker.internBruker(oidcLogin.getSubject());
            setLoginSuccess(true);
            logger.trace("Login successful for user {}", sluttBruker);
            return true;
        }
        if (loginResult == OidcLogin.LoginResult.ID_TOKEN_EXPIRED) {
            throw new CredentialExpiredException();
        }
        if (oidcLogin.getErrorMessage() != null) {
            throw new LoginException(oidcLogin.getErrorMessage());
        }
        throw new LoginException(loginResult.name());
    }

    @Override
    public void doCommit() throws LoginException {
        authenticationLevelCredential = new AuthenticationLevelCredential(AUTHENTICATION_LEVEL_INTERN_BRUKER);
        String mdcConsumerId = MDCOperations.getConsumerId();
        if (mdcConsumerId != null) {
            this.consumerId = new ConsumerId(mdcConsumerId);
        } else {
            this.consumerId = new ConsumerId();
        }
        oidcCredential = new OidcCredential(ssoToken.getToken());

        subject.getPrincipals().add(sluttBruker);
        subject.getPrincipals().add(this.consumerId);
        subject.getPublicCredentials().add(authenticationLevelCredential);
        subject.getPublicCredentials().add(oidcCredential);

        logger.trace("Login committed for user {}", sluttBruker);
    }

    @Override
    protected void cleanUpSubject() {
        if (!subject.isReadOnly()) {
            subject.getPrincipals().remove(sluttBruker);
            subject.getPrincipals().remove(consumerId);
            subject.getPublicCredentials().remove(authenticationLevelCredential);
            subject.getPublicCredentials().remove(oidcCredential);
        }
    }

    @Override
    protected void cleanUpLocalState() throws LoginException {
        // Set during login()
        ssoToken = null;

        if (sluttBruker != null) {
            sluttBruker.destroy();
        }
        sluttBruker = null;

        // Set during commit()
        if (consumerId != null) {
            consumerId.destroy();
        }
        consumerId = null;

        if (authenticationLevelCredential != null) {
            authenticationLevelCredential.destroy();
        }
        authenticationLevelCredential = null;

        if (oidcCredential != null) {
            oidcCredential.destroy();
        }
        oidcCredential = null;
    }

    /*
     * Called by login() to acquire the ID Token.
     */
    protected OidcTokenHolder getSSOToken() throws LoginException {
        logger.trace("Getting the SSO-token from callback");

        if (callbackHandler == null) {
            throw new LoginException("No callbackhandler provided");
        }

        TokenCallback tokenCallback = new TokenCallback();

        try {
            callbackHandler.handle(new Callback[] { tokenCallback });
            return tokenCallback.getToken();
        } catch (IOException | UnsupportedCallbackException e) {
            logger.debug("Error while handling getting token from callbackhandler: ", e);
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
    }

}