package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Inntektsperiode(InntektsperiodeKode kode, String termnavn) {


    @Deprecated(since = "4.0.x", forRemoval = true)
    public InntektsperiodeKode getKode() {
        return kode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public String getTermnavn() {
        return termnavn();
    }


}
