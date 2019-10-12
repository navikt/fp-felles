package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import no.nav.abac.common.xacml.CommonAttributter;
import no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggId;
import no.nav.vedtak.log.sporingslogg.StandardSporingsloggId;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

/**
 * Legacy eksempel på AbacSporingslogg implementasjon.
 *
 */
class LegacyAbacSporingslogg implements AbacSporingslogg {

    private static final Logger SPORINGSLOGG = AppLoggerFactory.getSporingLogger(LegacyAbacSporingslogg.class);

    private static final char SPACE_SEPARATOR = ' ';

    private static <T> List<Sporingsdata> utvidSporingsdata(List<Sporingsdata> input, Collection<T> attributtVerdier, SporingsloggId id) {
        if (attributtVerdier.isEmpty()) {
            return input;
        }
        List<Sporingsdata> output = new ArrayList<>();
        for (Sporingsdata sporingsdata : input) {
            int i = 1;
            for (T attributtVerdi : attributtVerdier) {
                Sporingsdata sd = i++ < attributtVerdier.size()
                    ? sporingsdata.kopi()
                    : sporingsdata;
                output.add(sd.leggTilId(id, attributtVerdi.toString()));
            }
        }
        return output;
    }

    void loggDeny(PdpRequest pdpRequest, List<Decision> decisions, AbacAttributtSamling attributter) {
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < decisions.size(); i++) {
            if (decisions.get(i) == Decision.Deny) {
                pdpRequestSporingsdata.add(byggSporingsdata(attributter.getAction(), pdpRequest, i, decisions.get(i)));
            }
        }

        List<Sporingsdata> sporingsdata = byggSammensattSporingsdata(attributter, pdpRequestSporingsdata, antallSporingsrader(attributter));
        logg(sporingsdata);
    }

    void loggTilgang(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        List<Sporingsdata> sporingsdata = byggSporingsdata(pdpRequest, attributter);
        logg(sporingsdata);
    }

    private List<Sporingsdata> byggSporingsdata(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        int antallRaderFraPdpRequest = antallResources(pdpRequest);
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < antallRaderFraPdpRequest; i++) {
            pdpRequestSporingsdata.add(byggSporingsdata(attributter.getAction(), pdpRequest, i));
        }

        int antallRaderFraAttributter = antallSporingsrader(attributter);
        return antallRaderFraAttributter == 1 || antallRaderFraPdpRequest == 1
            ? byggSammensattSporingsdata(attributter, pdpRequestSporingsdata, antallRaderFraAttributter)
            : byggIkkeSammensatteSporingsdata(attributter, pdpRequestSporingsdata, antallRaderFraAttributter);
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, antallAksjonspunktTyper(pdpRequest));
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }

    private List<Sporingsdata> byggIkkeSammensatteSporingsdata(AbacAttributtSamling attributter, List<Sporingsdata> pdpRequestSporingsdata, int antallRaderFraAttributter) {
        //egne linjer, for å unngå store kryssprodukter
        List<Sporingsdata> resultat = new ArrayList<>();
        resultat.addAll(pdpRequestSporingsdata);
        resultat.addAll(leggPåAttributter(Sporingsdata.opprett(attributter.getAction()), attributter, antallRaderFraAttributter));
        return resultat;
    }

    private List<Sporingsdata> byggSammensattSporingsdata(AbacAttributtSamling attributter, List<Sporingsdata> pdpRequestSporingsdata, int antallRaderFraAttributter) {
        //logg på samme linje(r)
        List<Sporingsdata> resultat = new ArrayList<>();
        for (Sporingsdata sporingsdatum : pdpRequestSporingsdata) {
            resultat.addAll(leggPåAttributter(sporingsdatum, attributter, antallRaderFraAttributter));
        }
        return resultat;
    }

    private List<Sporingsdata> leggPåAttributter(Sporingsdata original, AbacAttributtSamling attributter, int antallRaderFraAttributter) {
        List<Sporingsdata> sporingsdata = new ArrayList<>(antallRaderFraAttributter);
        sporingsdata.add(original);

        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.AKSJONSPUNKT_KODE), StandardSporingsloggId.AKSJONSPUNKT_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID), StandardSporingsloggId.AKTOR_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.BEHANDLING_ID), StandardSporingsloggId.BEHANDLING_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.BEHANDLING_UUID), StandardSporingsloggId.BEHANDLING_UUID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.DOKUMENT_DATA_ID), StandardSporingsloggId.DOKUMENT_DATA_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER), StandardSporingsloggId.GSAK_SAKSNUMMER);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getVerdier(StandardAbacAttributtType.JOURNALPOST_ID), StandardSporingsloggId.JOURNALPOST_ID);

        return sporingsdata;

    }

    private int antallSporingsrader(AbacAttributtSamling attributter) {
        return attributter.kryssProduktAntallAttributter();
    }

    private Sporingsdata byggSporingsdata(String action, PdpRequest pdpRequest, int index, Decision decision) {
        return byggSporingsdata(action, pdpRequest, index).leggTilId(StandardSporingsloggId.ABAC_DECISION, decision.getEksternKode());
    }

    private Sporingsdata byggSporingsdata(String action, PdpRequest pdpRequest, int index) {
        Sporingsdata sporingsdata = Sporingsdata.opprett(action)
            .leggTilId(StandardSporingsloggId.ABAC_ACTION, pdpRequest.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID))
            .leggTilId(StandardSporingsloggId.ABAC_RESOURCE_TYPE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        //hent ut fnr og aksjonpspunkt-typer vha indexer pga kryssprodukt mellom disse
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_FNR, index % Math.max(pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR), 1), StandardSporingsloggId.FNR);
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, index % Math.max(pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE), 1), StandardSporingsloggId.AKTOR_ID);
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, (index / Math.max(antallIdenter(pdpRequest), 1)), StandardSporingsloggId.ABAC_AKSJONSPUNKT_TYPE);

        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, StandardSporingsloggId.ABAC_ANSVALIG_SAKSBEHANDLER);
        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, StandardSporingsloggId.ABAC_BEHANDLING_STATUS);
        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, StandardSporingsloggId.ABAC_SAK_STATUS);
        return sporingsdata;
    }


    private void setOptionalValueinAttributeSet(Sporingsdata sporingsdata, PdpRequest pdpRequest, String key, SporingsloggId id) {
        pdpRequest.getOptional(key).ifPresent(s -> sporingsdata.leggTilId(id, s));
    }

    private void setOptionalListValueinAttributeSet(Sporingsdata sporingsdata, PdpRequest pdpRequest, String key, int index, SporingsloggId id) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            Optional.ofNullable(list.get(index)).ifPresent(s -> sporingsdata.leggTilId(id, s));
        }
    }

    @Override
    public void logg(List<Sporingsdata> sporingsdata) {
        for (Sporingsdata sporingsdatum : sporingsdata) {
            logg(sporingsdatum);
        }
    }

    private void logg(Sporingsdata sporingsdata) {
        StringBuilder msg = new StringBuilder()
            .append("action=").append(sporingsdata.getAction()).append(SPACE_SEPARATOR);
        for (SporingsloggId id : sporingsdata.getNøkler()) {
            String nøkkel = id.getSporingsloggKode();
            String verdi = sporingsdata.getVerdi(id);
            msg.append(nøkkel)
                .append('=')
                .append(fjernMellomrom(verdi))
                .append(SPACE_SEPARATOR);
        }
        String sanitizedMsg = LoggerUtils.toStringWithoutLineBreaks(msg);
        SPORINGSLOGG.info(sanitizedMsg); //NOSONAR
    }

    private String fjernMellomrom(String verdi) {
        return verdi != null ? verdi.replace(' ', '_') : null;
    }

    @Override
    public List<Sporingsdata> byggSporingsdata(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
        return byggSporingsdata(beslutning.getPdpRequest(), attributter);
    }

    @Override
    public void loggDeny(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
        loggDeny(beslutning.getPdpRequest(), beslutning.getDelbeslutninger(), attributter);
    }

}
