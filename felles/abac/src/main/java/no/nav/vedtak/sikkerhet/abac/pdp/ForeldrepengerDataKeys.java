package no.nav.vedtak.sikkerhet.abac.pdp;

import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_AKSJONSPUNKT_TYPE;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_ALENEOMSORG;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_ANNEN_PART;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_BEHANDLING_STATUS;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_SAKSBEHANDLER;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_SAKSID;
import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_FP_SAK_STATUS;

public enum ForeldrepengerDataKeys implements RessursDataKey {

    SAKSBEHANDLER(RESOURCE_FP_SAKSBEHANDLER),
    // Skal kun tilordnes spesifikke verdier fra enums under pdp.verdi...
    AKSJONSPUNKT_OVERSTYRING(RESOURCE_FP_AKSJONSPUNKT_TYPE),
    BEHANDLING_STATUS(RESOURCE_FP_BEHANDLING_STATUS),
    FAGSAK_STATUS(RESOURCE_FP_SAK_STATUS),

    // Selvbetjening
    ALENEOMSORG(RESOURCE_FP_ALENEOMSORG), // Boolean.tostring
    ANNENPART(RESOURCE_FP_ANNEN_PART),  // AktørId format

    // Ikke i bruk - mangler attributefinder + tilsv for behandlingUuid
    SAKSID(RESOURCE_FP_SAKSID),

    ;
    private final String key;

    ForeldrepengerDataKeys(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
