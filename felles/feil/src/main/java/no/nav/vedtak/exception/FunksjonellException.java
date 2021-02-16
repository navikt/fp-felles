package no.nav.vedtak.exception;

import static java.lang.String.format;

import no.nav.vedtak.feil.FunksjonellFeil;
import no.nav.vedtak.feil.LogLevel;

public class FunksjonellException extends VLException {

    public FunksjonellException(String kode, String msg, String hint) {
        this(kode, msg, hint, null);
    }

    public FunksjonellException(String kode, String msg, String hint, Throwable t) {
        this(new FunksjonellFeil(kode, kode, hint, LogLevel.WARN, FunksjonellException.class, t));
    }

    public FunksjonellException(String kode, String msg, Throwable t, Object... args) {
        this(kode, msg, null, LogLevel.WARN, t, args);
    }

    public FunksjonellException(String kode, String msg, String hint, LogLevel level, Throwable cause, Object... args) {
        this(new FunksjonellFeil(kode, format(msg, args), hint, level, FunksjonellException.class, cause));
    }

    /**
     *
     * @deprecated Lag med new og args.
     */
    @Deprecated
    public FunksjonellException(FunksjonellFeil feil) {
        super(feil);
    }

    @Override
    public FunksjonellFeil getFeil() {
        return FunksjonellFeil.class.cast(super.getFeil());
    }
}
