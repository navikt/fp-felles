package no.nav.vedtak.exception;

import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class IntegrasjonException extends VLException {

    private static final String DEFAULT_MSG = "Server [%s] svarte med feilkode http-kode '%s' og response var '%s'";

    public IntegrasjonException(URI endpoint, int status, String reason) {
        this(DEFAULT_MSG, endpoint, status, reason);
    }

    public IntegrasjonException(String msg, Object... args) {
        this(format(msg, args), LogLevel.WARN, null);
    }

    private IntegrasjonException(String msg, LogLevel level, Throwable cause) {
        this(new Feil("F-686912", msg, level, IntegrasjonException.class, cause));
    }

    public IntegrasjonException(Feil feil) {
        super(feil);
    }

}
