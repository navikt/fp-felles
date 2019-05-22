package no.nav.vedtak.sikkerhet.abac;

/**
 * @deprecated Flyttes til fpsak. Styr unna
 */
@Deprecated
public enum AbacFagsakStatus {
    OPPRETTET("Opprettet"),
    UNDER_BEHANDLING("Under behandling");

    private String eksternKode;

    AbacFagsakStatus(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
