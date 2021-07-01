package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Arbeidskategori(ArbeidskategoriKode kode, String termnavn) {


    @Deprecated
    public ArbeidskategoriKode getKode() {
        return kode();
    }

    @Deprecated
    public String getTermnavn() {
        return termnavn();
    }
}
