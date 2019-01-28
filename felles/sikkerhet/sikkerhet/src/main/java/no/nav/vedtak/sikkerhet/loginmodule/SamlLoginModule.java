package no.nav.vedtak.sikkerhet.loginmodule;

import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import java.util.Map;

/**
 * <p> This <code>LoginModule</code> authenticates users using
 * the custom SAML token.
 */
public class SamlLoginModule extends LoginModuleBase {

    private static Logger logger = LoggerFactory.getLogger(SamlLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;

    private SamlInfo samlInfo;
    private Assertion samlAssertion;

    private SluttBruker sluttBruker;
    private AuthenticationLevelCredential authenticationLevelCredential;
    private SAMLAssertionCredential samlAssertionCredential;
    private ConsumerId consumerId;

    public SamlLoginModule() {
        super(logger);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        logger.trace("Initialize loginmodule");
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        logger.trace("Initializing with subject: {} callbackhandler: {}", subject, callbackHandler);
    }

    @Override
    public boolean login() throws LoginException {
        try {
            logger.trace("enter login");
            PasswordCallback passwordCallback = new PasswordCallback("Return SAML-assertion as password", false);
            callbackHandler.handle(new Callback[] { passwordCallback });

            samlAssertion = SamlUtils.toSamlAssertion(new String(passwordCallback.getPassword()));
            samlInfo = SamlUtils.getSamlInfo(samlAssertion);
            setLoginSuccess(true);
            logger.trace("Login successful for user {} with authentication level {}", samlInfo.getUid(), samlInfo.getAuthLevel());
            return true;
        } catch (Exception e) {
            samlAssertion = null;
            samlInfo = null;
            logger.trace("leave login: exception");
            throw new LoginException(e.toString());// NOPMD
        }
    }

    @Override
    public void doCommit() throws LoginException {
        sluttBruker = new SluttBruker(samlInfo.getUid(), getIdentType());
        authenticationLevelCredential = new AuthenticationLevelCredential(samlInfo.getAuthLevel());
        samlAssertionCredential = new SAMLAssertionCredential(samlAssertion.getDOM());
        consumerId = new ConsumerId(samlInfo.getConsumerId());

        subject.getPrincipals().add(sluttBruker);
        subject.getPrincipals().add(consumerId);
        subject.getPublicCredentials().add(authenticationLevelCredential);
        subject.getPublicCredentials().add(samlAssertionCredential);

        logger.trace("Login committed for subject with uid: {} authentication level: {} and consumerId: {}",
                sluttBruker.getName(), authenticationLevelCredential.getAuthenticationLevel(), consumerId);
    }

    private IdentType getIdentType() throws LoginException {
        IdentType identType;
        try {
            identType = IdentType.valueOf(samlInfo.getIdentType());
        } catch (IllegalArgumentException e) {
            LoginException le = new LoginException("Could not commit. Unknown ident type: " + samlInfo.getIdentType() + " " + e);
            le.initCause(e);
            throw le;
        }
        return identType;
    }

    @Override
    protected void cleanUpSubject(){
        if(!subject.isReadOnly()){
            subject.getPrincipals().remove(sluttBruker);
            subject.getPrincipals().remove(consumerId);
            subject.getPublicCredentials().remove(samlAssertionCredential);
            subject.getPublicCredentials().remove(authenticationLevelCredential);
        }
    }

    @Override
    protected void cleanUpLocalState() throws LoginException {
        // Set during login()
        samlInfo = null;
        samlAssertion = null;

        // Set during commit()
        if (sluttBruker != null) {
            sluttBruker.destroy();
        }
        sluttBruker = null;

        if (consumerId != null) {
            consumerId.destroy();
        }
        consumerId = null;

        if (authenticationLevelCredential != null) {
            authenticationLevelCredential.destroy();
        }
        authenticationLevelCredential = null;

        if (samlAssertionCredential != null) {
            samlAssertionCredential.destroy();
        }
        samlAssertionCredential = null;
    }
}
