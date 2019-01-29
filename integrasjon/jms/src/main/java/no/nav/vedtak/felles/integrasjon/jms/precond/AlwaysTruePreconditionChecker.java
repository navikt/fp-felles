package no.nav.vedtak.felles.integrasjon.jms.precond;

public class AlwaysTruePreconditionChecker implements PreconditionChecker {

    @Override
    public PreconditionCheckerResult check() {
        return PreconditionCheckerResult.fullfilled();
    }
}
