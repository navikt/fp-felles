package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feilkode;

public abstract sealed class VLException extends RuntimeException
    permits FunksjonellException, IntegrasjonException, ManglerTilgangException, TekniskException {

    private final String kode;
    private final String msg;
    private final Throwable cause;

    protected VLException(String kode, String msg, Throwable cause) {
        super(kode != null ? kode + ": " + msg : msg, cause);
        this.kode = kode;
        this.msg = msg;
        this.cause = cause;
    }

    public String getFeilkode() {
        return Feilkode.GENERELL.name();
    }

    public String getKode() {
        return kode;
    }

    public String getFeilmelding() {
        return msg;
    }

    public abstract int getStatusCode();

    public VLLogLevel getLogLevel() {
        return VLLogLevel.WARN;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [kode=" + kode + ", msg=" + msg + ", cause=" + cause + "]";
    }

}
