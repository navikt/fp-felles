package no.nav.vedtak.felles.integrasjon.jms.pausing;

class LinearBackoffHandler extends ErrorHandler {

    LinearBackoffHandler(long initialWait, int maxFailed) {
        super(initialWait, maxFailed);
    }

    @Override
    public long getNextPauseLengthInMillisecs() {
        long attempt = getFailedAttempts();
        incrementFailedAttempts();
        long wait = getInitialWait();
        wait += Math.multiplyExact(getInitialWait(), Long.min(getMaxFailed(), attempt));
        return wait;
    }
}
