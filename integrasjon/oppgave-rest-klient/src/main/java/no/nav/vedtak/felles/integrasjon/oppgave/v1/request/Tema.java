package no.nav.vedtak.felles.integrasjon.oppgave.v1.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Tema {

    FOR("FOR", "Foreldre- og svangerskapspenger"), //$NON-NLS-1$ //$NON-NLS-2$
    SYK("SYK", "Sykepenger"), //$NON-NLS-1$ //$NON-NLS-2$
    OMS("OMS", "Omsorg-, pleie- og opplæringspenger"), //$NON-NLS-1$ //$NON-NLS-2$
    FOS("FOS", "Forsikring"), //$NON-NLS-1$ //$NON-NLS-2$
    TSO("TSO", "Tilleggsstønad"), //$NON-NLS-1$ //$NON-NLS-2$
    STO("STO", "Regnskap/utbetaling"); //$NON-NLS-1$ //$NON-NLS-2$

    @JsonIgnore
    private String beskrivelse;
    @JsonValue
    private String kode;

    Tema(String kode, String beskrivelse) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
    }

    public static Tema fraString(String kode) {
        for (Tema key : values()) {
            if (key.getKode().equals(kode)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Finner ikke fagområdekode med key: " + kode); //$NON-NLS-1$
    }

    public String getKode() {
        return kode;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }
}
