package no.nav.vedtak.felles.integrasjon.jms.pausing;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Vente-strategi for (forbigående) feilsituasjoner.
 */
public abstract class ErrorHandler {

    private final long initialWait;
    private final int maxFailed;
    private AtomicLong failedAttempts = new AtomicLong();

    ErrorHandler(long initialWait, int maxFailed) {
        this.initialWait = initialWait;
        this.maxFailed = maxFailed;
    }

    public abstract long getNextPauseLengthInMillisecs();

    public void reset() {
        failedAttempts.set(0L);
    }

    public long getFailedAttempts() {
        return failedAttempts.get();
    }

    protected long incrementFailedAttempts() {
        return failedAttempts.incrementAndGet();
    }

    public int getMaxFailed() {
        return maxFailed;
    }

    public long getInitialWait() {
        return initialWait;
    }

}
