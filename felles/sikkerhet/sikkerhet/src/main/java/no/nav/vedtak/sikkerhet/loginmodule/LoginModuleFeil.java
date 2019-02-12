package no.nav.vedtak.sikkerhet.loginmodule;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import javax.security.auth.login.LoginException;

public interface LoginModuleFeil extends DeklarerteFeil {

    LoginModuleFeil FACTORY = FeilFactory.create(LoginModuleFeil.class);

    @TekniskFeil(feilkode = "F-651753", feilmelding = "Kunne ikke finne konfigurasjonen for %s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeFinneLoginmodulen(String name, LoginException le);

    @TekniskFeil(feilkode = "F-727999", feilmelding = "Noe gikk galt ved utlogging", logLevel = LogLevel.WARN)
    Feil feiletUtlogging(LoginException e);

    @TekniskFeil(feilkode = "F-499051", feilmelding = "Noe gikk galt ved innlogging", logLevel = LogLevel.ERROR)
    Feil feiletInnlogging(Exception le);
}
