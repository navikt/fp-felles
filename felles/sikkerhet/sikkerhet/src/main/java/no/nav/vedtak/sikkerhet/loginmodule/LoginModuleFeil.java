package no.nav.vedtak.sikkerhet.loginmodule;

import javax.security.auth.login.LoginException;

import no.nav.vedtak.exception.TekniskException;

public class LoginModuleFeil {
    private LoginModuleFeil() {

    }

    public static TekniskException kunneIkkeFinneLoginmodulen(String name, LoginException e) {
        return new TekniskException("F-651753", String.format("Kunne ikke finne konfigurasjonen for %s", name), e);
    }

    static TekniskException feiletUtlogging(LoginException e) {
        return new TekniskException("F-727999", "Noe gikk galt ved utlogging", e);

    }

    static TekniskException feiletInnlogging(Exception e) {
        return new TekniskException("F-499051", "Noe gikk galt ved innlogging", e);
    }
}
