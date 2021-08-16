package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Arbeidskategori(ArbeidskategoriKode kode, String termnavn) {


    @Deprecated(since = "4.0.x", forRemoval = true)
    public ArbeidskategoriKode getKode() {
        return kode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public String getTermnavn() {
        return termnavn();
    }
}
