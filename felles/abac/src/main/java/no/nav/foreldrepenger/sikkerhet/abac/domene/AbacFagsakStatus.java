package no.nav.foreldrepenger.sikkerhet.abac.domene;

public enum AbacFagsakStatus {
    OPPRETTET("Opprettet"),
    UNDER_BEHANDLING("Under behandling");

    private final String eksternKode;

    AbacFagsakStatus(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
