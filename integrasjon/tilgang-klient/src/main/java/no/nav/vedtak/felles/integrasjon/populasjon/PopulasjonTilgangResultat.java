package no.nav.vedtak.felles.integrasjon.populasjon;

public enum PopulasjonTilgangResultat {
    GODKJENT,
    AVSLÅTT_KODE_7,
    AVSLÅTT_KODE_6,
    AVSLÅTT_EGEN_ANSATT,
    AVSLÅTT_ANNEN_ÅRSAK;

    public boolean fikkTilgang() {
        return this == GODKJENT;
    }
}
