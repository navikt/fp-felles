package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class IntegrasjonException extends VLException {

    public IntegrasjonException(String kode, String msg) {
        this(kode, msg, null);
    }

    public IntegrasjonException(String kode, String msg, Throwable cause) {
        this(kode, msg, LogLevel.WARN, cause);
    }

    private IntegrasjonException(String kode, String msg, LogLevel level, Throwable cause) {
        this(new Feil(kode, msg, level, IntegrasjonException.class, cause));
    }

    /**
     *
     * @deprecated Lag med new
     */
    @Deprecated(since = "3.0.x", forRemoval = true)
    public IntegrasjonException(Feil feil) {
        super(feil);
    }

}
