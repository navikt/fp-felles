package no.nav.vedtak.exception;

import no.nav.vedtak.feil.FunksjonellFeil;
import no.nav.vedtak.feil.LogLevel;

public class FunksjonellException extends VLException {

    public FunksjonellException(String kode, String msg) {
        this(kode, msg, null);
    }

    public FunksjonellException(String kode, String msg, String hint) {
        this(kode, msg, hint, null);
    }

    public FunksjonellException(String kode, String msg, String hint, Throwable t) {
        this(new FunksjonellFeil(kode, msg, hint, LogLevel.WARN, FunksjonellException.class, t));
    }

    /**
     *
     * @deprecated Lag med new
     */
    @Deprecated(since = "3.0.x", forRemoval = true)
    public FunksjonellException(FunksjonellFeil feil) {
        super(feil);
    }

    @Override
    @Deprecated
    public FunksjonellFeil getFeil() {
        return FunksjonellFeil.class.cast(super.getFeil());
    }
}
