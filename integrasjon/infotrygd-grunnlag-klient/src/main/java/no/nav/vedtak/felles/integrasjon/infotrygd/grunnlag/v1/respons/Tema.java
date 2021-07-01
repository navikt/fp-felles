package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Tema(TemaKode kode, String termnavn) {

    @Deprecated
    public TemaKode getKode() {
        return kode();
    }

    @Deprecated
    public String getTermnavn() {
        return termnavn();
    }
}
