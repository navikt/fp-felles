package no.nav.vedtak.exception;

import static java.lang.String.format;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;

public class TekniskException extends VLException {

    private static final String DEFAULT_MSG = "Feil ved kommunikasjon med server [%s]";

    public TekniskException(String kode, String msg) {
        this(kode, msg, (URI) null);
    }

    public TekniskException(String kode, String msg, Throwable cause) {
        this(kode, msg, WARN, TekniskException.class, cause);
    }

    public TekniskException(String kode, String msg, URI server) {
        this(kode, msg, server, null);
    }

    public TekniskException(String kode, String msg, URI server, Throwable cause) {
        this(kode, format(msg, server), LogLevel.WARN, TekniskException.class, cause);
    }

    public TekniskException(String kode, URI server, Throwable cause) {
        this(kode, server, WARN, cause);
    }

    public TekniskException(String kode, URI server, LogLevel level, Throwable cause) {
        this(kode, server, level, TekniskException.class, cause);
    }

    public TekniskException(String kode, URI server, LogLevel level, Class<? extends VLException> clazz, Throwable cause) {
        this(kode, format(DEFAULT_MSG, server), level, clazz, cause);
    }

    public TekniskException(String kode, String msg, LogLevel level, Class<? extends VLException> clazz, Throwable cause) {
        this(new Feil(kode, msg, level, clazz, cause));
    }

    /**
     *
     * @deprecated Lag med new og args.
     */
    @Deprecated
    public TekniskException(Feil feil) {
        super(feil);
    }
}
