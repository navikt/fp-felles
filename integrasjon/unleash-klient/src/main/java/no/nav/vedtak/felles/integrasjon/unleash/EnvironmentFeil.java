package no.nav.vedtak.felles.integrasjon.unleash;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface EnvironmentFeil extends DeklarerteFeil {

    EnvironmentFeil FACTORY = FeilFactory.create(EnvironmentFeil.class);

    @TekniskFeil(feilkode = "F-350857", feilmelding = "Mangler property " + EnvironmentProperty.APP_NAME, logLevel = LogLevel.ERROR)
    Feil manglerApplicationNameProperty();
}