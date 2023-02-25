package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Oppgavetype {

    JOURNALFØRING("JFR"),
    VURDER_KONSEKVENS_YTELSE("VUR_KONS_YTE"),
    VURDER_DOKUMENT("VUR"),
    BEHANDLE_SAK("BEH_SAK"),
    FEILUTBETALING("FEILUTBET"),
    INNHENT_DOK("INNH_DOK"),
    SETT_UTBETALING_VENT("SETTVENT");


    @JsonValue
    private String kode;

    Oppgavetype(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
