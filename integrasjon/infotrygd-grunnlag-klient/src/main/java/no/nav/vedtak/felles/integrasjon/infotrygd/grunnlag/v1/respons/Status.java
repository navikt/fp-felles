package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Status(StatusKode kode, String termnavn) {

    @Deprecated
    public StatusKode getKode() {
        return kode();
    }

    @Deprecated
    public String getTermnavn() {
        return termnavn();
    }
}
