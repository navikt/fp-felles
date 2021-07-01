package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Inntektsperiode(InntektsperiodeKode kode, String termnavn) {


    @Deprecated
    public InntektsperiodeKode getKode() {
        return kode();
    }

    @Deprecated
    public String getTermnavn() {
        return termnavn();
    }


}
