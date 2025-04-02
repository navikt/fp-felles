package no.nav.vedtak.sikkerhet.abac.pdp;

public enum ForeldrepengerDataKeys {

    SAKSBEHANDLER,
    // Skal kun tilordnes spesifikke verdier fra enums under pdp.verdi...
    AKSJONSPUNKT_OVERSTYRING,
    BEHANDLING_STATUS,
    FAGSAK_STATUS,

    // Selvbetjening
    ALENEOMSORG, // Boolean.tostring
    ANNENPART,  // Personident (AktørId eller FNR) for den som er annenpart i aktuell sak

    // LOS
    AVDELING_ENHET

}
