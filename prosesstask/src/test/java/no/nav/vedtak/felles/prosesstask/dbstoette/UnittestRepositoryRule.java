package no.nav.vedtak.felles.prosesstask.dbstoette;

import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class UnittestRepositoryRule extends RepositoryRule {

    static {
        Databaseskjemainitialisering.kjørMigreringHvisNødvendig();
        Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag();
    }

    public UnittestRepositoryRule() {
        super();
    }

    public UnittestRepositoryRule(boolean transaksjonell) {
        super(transaksjonell);
    }

    public UnittestRepositoryRule(String persistenceUnitKey) {
        super(persistenceUnitKey);
    }

    public UnittestRepositoryRule(String persistenceUnitKey, boolean transaksjonell) {
        super(persistenceUnitKey, transaksjonell);
    }

    @Override
    protected void init() {
    }

}
