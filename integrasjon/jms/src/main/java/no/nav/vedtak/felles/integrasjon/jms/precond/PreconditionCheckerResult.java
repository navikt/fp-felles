package no.nav.vedtak.felles.integrasjon.jms.precond;

import java.util.Optional;

public final class PreconditionCheckerResult {

    private boolean isFulfilled;
    private Optional<String> errorMessage;

    private PreconditionCheckerResult(boolean isFulfilled, String errorMessage) {
        this.isFulfilled = isFulfilled;
        this.errorMessage = Optional.ofNullable(errorMessage);
    }

    public static PreconditionCheckerResult fullfilled() {
        return new PreconditionCheckerResult(true, null);
    }

    public static PreconditionCheckerResult notFullfilled(String errorMessage) {
        return new PreconditionCheckerResult(false, errorMessage);
    }

    public boolean isFulfilled() {
        return isFulfilled;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
