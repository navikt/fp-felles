package no.nav.vedtak.felles.integrasjon.behandleoppgave;

public enum FagomradeKode {

    FOR("FOR", "Foreldre- og svangerskapspenger"), //$NON-NLS-1$ //$NON-NLS-2$
    SYK("SYK", "Sykepenger"), //$NON-NLS-1$ //$NON-NLS-2$
    OMS("OMS", "Omsorg-, pleie- og opplæringspenger"), //$NON-NLS-1$ //$NON-NLS-2$
    FOS("FOS", "Forsikring"), //$NON-NLS-1$ //$NON-NLS-2$
    TSO("TSO", "Tilleggsstønad"), //$NON-NLS-1$ //$NON-NLS-2$
    STO("STO", "Regnskap/utbetaling"), //$NON-NLS-1$ //$NON-NLS-2$
    UKJ("UKJ", "Ukjent"); // for tester //$NON-NLS-1$ //$NON-NLS-2$

    private String beskrivelse;
    private String kode;

    FagomradeKode(String kode, String beskrivelse) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
    }

    public String getKode() {
        return kode;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public static FagomradeKode fraString(String kode) {
        for (FagomradeKode key : values()) {
            if (key.getKode().equals(kode)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Finner ikke fagområdekode med key: " + kode); //$NON-NLS-1$
    }
}
