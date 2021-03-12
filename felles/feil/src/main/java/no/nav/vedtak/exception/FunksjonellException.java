package no.nav.vedtak.exception;

public class FunksjonellException extends VLException {

    private final String løsningsforslag;

    public FunksjonellException(String kode, String msg) {
        this(kode, msg, null);
    }

    public FunksjonellException(String kode, String msg, String hint) {
        this(kode, msg, hint, null);
    }

    public FunksjonellException(String kode, String msg, String løsningsforslag, Throwable t) {
        super(kode, msg, t);
        this.løsningsforslag = løsningsforslag;
    }

    public String getLøsningsforslag() {
        return løsningsforslag;
    }

}
