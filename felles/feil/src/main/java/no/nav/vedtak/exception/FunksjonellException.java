package no.nav.vedtak.exception;

public class FunksjonellException extends VLException {

    private final String hint;

    public FunksjonellException(String kode, String msg) {
        this(kode, msg, null);
    }

    public FunksjonellException(String kode, String msg, String hint) {
        this(kode, msg, hint, null);
    }

    public FunksjonellException(String kode, String msg, String hint, Throwable t) {
        super(kode, msg, t);
        this.hint = hint;
    }

}
