package no.nav.vedtak.exception;

import static java.lang.String.format;

import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class IntegrasjonException extends VLException {

    private static final String KODE = "F-686912";
    private static final String DEFAULT_MSG = "Server [%s] svarte med feilkode http-kode '%s' og response var '%s'";

    public IntegrasjonException(Throwable t, URI uri) {
        this(KODE, t, uri);
    }

    public IntegrasjonException(URI endpoint, int status, String reason) {
        this(KODE, DEFAULT_MSG, LogLevel.WARN, null, endpoint, status, reason);
    }

    public IntegrasjonException(String kode, Throwable t, Object... args) {
        this(kode, DEFAULT_MSG, t, args);
    }

    public IntegrasjonException(String kode, String msg, Throwable t, Object... args) {
        this(kode, msg, LogLevel.WARN, t, args);
    }

    public IntegrasjonException(String msg, Object... args) {
        this(KODE, msg, LogLevel.WARN, null, args);
    }

    public IntegrasjonException(String kode, String msg, LogLevel level, Throwable cause, Object... args) {
        this(new Feil(kode, format(msg, args), level, IntegrasjonException.class, cause));
    }

    /**
     *
     * @deprecated Lag med new og args.
     */
    @Deprecated
    public IntegrasjonException(Feil feil) {
        super(feil);
    }

}
