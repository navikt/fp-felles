package no.nav.vedtak.exception;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class TekniskException extends VLException {

    public TekniskException(String kode, String msg) {
        this(kode, msg, null);
    }

    public TekniskException(String kode, String msg, Throwable cause) {
        this(kode, msg, WARN, TekniskException.class, cause);
    }

    private TekniskException(String kode, String msg, LogLevel level, Class<? extends VLException> clazz, Throwable cause) {
        this(new Feil(kode, msg, level, clazz, cause));
    }

    /**
     *
     * @deprecated Lag med new
     */
    @Deprecated(since = "3.0.x", forRemoval = true)
    public TekniskException(Feil feil) {
        super(feil);
    }
}
