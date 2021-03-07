package no.nav.vedtak.felles.testutilities.db;

import java.sql.SQLException;

import org.flywaydb.core.api.FlywayException;

import no.nav.vedtak.exception.TekniskException;

public class DbMigreringFeil {

    private DbMigreringFeil() {

    }

    TekniskException flywayMigreringFeilet(FlywayException e) {
        return new TekniskException("F-891250", "Databasemigrering feilet. Kan ikke fortsette enhetstesting", e);
    }

    TekniskException kanIkkeDetektereDatbaseType(SQLException e) {
        return new TekniskException("F-891250", "Databasemigrering feilet. Kan ikke fortsette enhetstesting", e);
    }

}
