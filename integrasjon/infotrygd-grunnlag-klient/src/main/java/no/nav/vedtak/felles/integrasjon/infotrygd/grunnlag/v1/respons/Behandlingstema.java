package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Behandlingstema(BehandlingstemaKode kode, String termnavn) {

    @Deprecated
    public BehandlingstemaKode getKode() {
        return kode();
    }

    @Deprecated
    public String getTermnavn() {
        return termnavn();
    }
}
