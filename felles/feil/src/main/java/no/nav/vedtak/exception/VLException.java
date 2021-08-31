package no.nav.vedtak.exception;

public abstract class VLException extends RuntimeException {

    private final String kode;
    private final String msg;
    private final Throwable cause;

    protected VLException(String kode, String msg, Throwable cause) {
        super(kode + ":" + msg, cause);
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
