package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * <p>
 * Dummy LoginModule - finnes kun pga Jakarta Authentication 3.0 kap 6 LoginModule Bridge Profile
 * Autentisering foregår i SAM. Usikker om denne overhodet trengs
 * </p>
 */
public class DummyLoginModule implements LoginModule {


    public DummyLoginModule() {
        // NOOP
    }


    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        // NOOP
    }

    @Override
    public boolean login() throws LoginException {
        return true;
    }

    @Override
    public final boolean commit() {
        return true;
    }

    @Override
    public final boolean abort() {
        return true;
    }

    @Override
    public final boolean logout() {
        return true;
    }

}
