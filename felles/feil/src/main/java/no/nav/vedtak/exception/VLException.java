package no.nav.vedtak.exception;
@Deprecated(since = "3.1", forRemoval = true)
/* Bruk klasser fra no.nav.foreldrepenger.felles:feil:1.0.1 istedenfor. */
public abstract class VLException extends RuntimeException {

    private final String kode;
    private final String msg;
    private final Throwable cause;

    protected VLException(String kode, String msg, Throwable cause) {
        super(kode + ":" + msg);
        this.kode = kode;
        this.msg = msg;
        this.cause = cause;
    }

    public String getKode() {
        return kode;
    }

    public String getFeilmelding() {
        return msg;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [kode=" + kode + ", msg=" + msg + ", cause=" + cause + "]";
    }
}
