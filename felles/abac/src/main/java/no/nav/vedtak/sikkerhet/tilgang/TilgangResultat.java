package no.nav.vedtak.sikkerhet.tilgang;

public enum TilgangResultat {
    GODKJENT,
    AVSLÅTT_KODE_7,
    AVSLÅTT_KODE_6,
    AVSLÅTT_EGEN_ANSATT,
    AVSLÅTT_ANNEN_ÅRSAK;

    public boolean fikkTilgang() {
        return this == GODKJENT;
    }
}
