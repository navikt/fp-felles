package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Behandlingstema(BehandlingstemaKode kode, String termnavn) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    public BehandlingstemaKode getKode() {
        return kode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public String getTermnavn() {
        return termnavn();
    }
}
