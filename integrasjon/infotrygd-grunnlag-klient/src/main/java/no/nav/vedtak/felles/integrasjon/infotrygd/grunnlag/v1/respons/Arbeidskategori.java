package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Arbeidskategori(ArbeidskategoriKode kode, String termnavn) implements InfotrygdKode {

    @Override
    public String getKode() {
        return kode().getKode();
    }

}
