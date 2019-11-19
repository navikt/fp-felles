package no.nav.vedtak.felles.integrasjon.oppgave.v1.request;

public enum OppgaveType {
    JOURNALFØRING("JFR"),
    FORDELING("FDR"),
    VURDER_DOKUMENT("VUR");

    private String kode;

    OppgaveType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
