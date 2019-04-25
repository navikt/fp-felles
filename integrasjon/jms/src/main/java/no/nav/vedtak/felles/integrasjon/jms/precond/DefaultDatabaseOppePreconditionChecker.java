package no.nav.vedtak.felles.integrasjon.jms.precond;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

@ApplicationScoped
public class DefaultDatabaseOppePreconditionChecker implements PreconditionChecker {

    @Resource(mappedName = "jdbc/defaultDS")
    private DataSource dataSource;

    DefaultDatabaseOppePreconditionChecker() {
        // for CDI proxy
    }

    // for enhetstest
    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PreconditionCheckerResult check() {
        try (Connection connection = dataSource.getConnection()) {
            // Connection pool validerer connections for oss, så trenger ikke gjøre noen spørring her (ønsker
            // bare å se om db er tilgjengelig)
            return PreconditionCheckerResult.fullfilled();
        } catch (SQLException e) { // NOSONAR
            return PreconditionCheckerResult.notFullfilled(e.getMessage());
        }
    }
}
