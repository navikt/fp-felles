package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Oppgavetype {

    BEHANDLE_SAK("BEH_SAK_VL"),
    REVURDER("RV_VL"),
    GODKJENNE_VEDTAK("GOD_VED_VL"),
    REGISTRER_SØKNAD("REG_SOK_VL"),
    JOURNALFØRING("JFR"),
    VURDER_KONSEKVENS_YTELSE("VUR_KONS_YTE"),
    VURDER_DOKUMENT("VUR_VL"),
    FEILUTBETALING("FEILUTBET"),
    INNHENT_DOK("INNH_DOK"),
    SETT_UTBETALING_VENT("SETTVENT"),
    BEHANDLE_SAK_INFOTRYGD("BEH_SAK");


    @JsonValue
    private String kode;

    Oppgavetype(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
