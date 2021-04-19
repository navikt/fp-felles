package no.nav.foreldrepenger.sikkerhet.abac.domene;

public enum AbacBehandlingStatus {
    OPPRETTET("Opprettet"),
    UTREDES("Behandling utredes"),
    FATTE_VEDTAK("Kontroller og fatte vedtak");

    private final String eksternKode;

    AbacBehandlingStatus(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
