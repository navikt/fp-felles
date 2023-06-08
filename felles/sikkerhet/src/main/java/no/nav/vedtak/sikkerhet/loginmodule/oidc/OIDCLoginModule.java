package no.nav.vedtak.sikkerhet.loginmodule.oidc;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.TokenCallback;
import no.nav.vedtak.sikkerhet.context.containers.ConsumerId;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.loginmodule.LoginModuleBase;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

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

    private static final Logger LOG = LoggerFactory.getLogger(OIDCLoginModule.class);

    // Set during initialize()
    private Subject subject;
    private CallbackHandler callbackHandler;

    // Set during login()
    private OpenIDToken ssoToken;
    private SluttBruker sluttBruker;

    // Set during commit()
    private ConsumerId consumerId;

    public OIDCLoginModule() {
        super(LOG);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        ssoToken = getSSOToken();

        OidcLogin.Resultat resultat = OidcLogin.validerToken(ssoToken);
        if (OidcLogin.LoginResult.SUCCESS.equals(resultat.loginResult())) {
            sluttBruker = resultat.subject();
            setLoginSuccess(true);
            return true;
        }
        if (OidcLogin.LoginResult.ID_TOKEN_EXPIRED.equals(resultat.loginResult())) {
            throw new CredentialExpiredException();
        }
        if (resultat.errorMessage() != null) {
            throw new LoginException(resultat.errorMessage());
        }
        throw new LoginException(resultat.loginResult().name());
    }

    @Override
    public void doCommit() throws LoginException {
        String mdcConsumerId = MDCOperations.getConsumerId();
        if (mdcConsumerId != null) {
            this.consumerId = new ConsumerId(mdcConsumerId);
        } else {
            this.consumerId = new ConsumerId();
        }

        subject.getPrincipals().add(sluttBruker);
        subject.getPrincipals().add(this.consumerId);
    }

    @Override
    protected void cleanUpSubject() {
        if (!subject.isReadOnly()) {
            subject.getPrincipals().remove(sluttBruker);
            subject.getPrincipals().remove(consumerId);
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
    }

    /*
     * Called by login() to acquire the ID Token.
     */
    protected OpenIDToken getSSOToken() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("No callbackhandler provided");
        }

        TokenCallback tokenCallback = new TokenCallback();

        try {
            callbackHandler.handle(new Callback[]{tokenCallback});
            return tokenCallback.getToken();
        } catch (IOException | UnsupportedCallbackException e) {
            LOG.debug("Error while handling getting token from callbackhandler: ", e);
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
    }

}
