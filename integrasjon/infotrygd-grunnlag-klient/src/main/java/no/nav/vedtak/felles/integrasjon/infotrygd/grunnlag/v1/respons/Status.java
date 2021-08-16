package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Status(StatusKode kode, String termnavn) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    public StatusKode getKode() {
        return kode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public String getTermnavn() {
        return termnavn();
    }
}
