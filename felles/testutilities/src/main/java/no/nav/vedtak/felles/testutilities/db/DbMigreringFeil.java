package no.nav.vedtak.felles.testutilities.db;

import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

/**
 * Feilkoder for migreringsfeil under enhetstester.
 */
public interface DbMigreringFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "F-415128", logLevel = LogLevel.ERROR, feilmelding = "Databasemigrering feilet. Kan ikke fortsette enhetstesting")
    Feil flywayMigreringFeilet(FlywayException flywayException);

    @TekniskFeil(feilkode = "F-891250", logLevel = LogLevel.ERROR, feilmelding = "Databasemigrering feilet. Kan ikke fortsette enhetstesting")
    Feil kanIkkeDetektereDatbaseType(SQLException e);

}
