package no.nav.vedtak.exception;

@Deprecated(since = "3.1", forRemoval = true)
/* Bruk klasser fra no.nav.foreldrepenger.felles:feil:1.0.1 istedenfor. */
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
