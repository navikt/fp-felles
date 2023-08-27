package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import no.nav.vedtak.sikkerhet.context.containers.BrukerNavnType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

/**
 * <p>
 * Dummy LoginModule - finnes kun pga Jakarta Authentication 3.0 kap 6 LoginModule Bridge Profile
 * Autentisering foregår i SAM. Usikker om denne overhodet trengs
 * </p>
 */
public class DummyLoginModule implements LoginModule {

    private Subject subject;

    public DummyLoginModule() {
        // NOOP
    }


    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
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
        return this.logout();
    }

    @Override
    public final boolean logout() {
        ryddKontekster();
        return true;
    }

    private void ryddKontekster() {
        if (KontekstHolder.harKontekst()) {
            KontekstHolder.fjernKontekst();
        }
        Optional.ofNullable(subject).map(Subject::getPrincipals).orElseGet(Set::of).stream()
            .filter(BrukerNavnType.class::isInstance)
            .forEach(p -> subject.getPrincipals().remove(p));
    }

}
