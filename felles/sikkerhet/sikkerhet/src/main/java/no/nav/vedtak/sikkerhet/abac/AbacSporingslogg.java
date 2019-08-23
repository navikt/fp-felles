package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.abac.xacml.CommonAttributter;
import org.slf4j.Logger;


import no.nav.abac.xacml.ForeldrepengerAttributter;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggId;
import no.nav.vedtak.log.sporingslogg.StandardSporingsloggId;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

/**
 * @deprecated FIXME Denne klassen har Høy teknisk gjeld og er en flaskehals hver gang noen har noen nytt å spore. Bør skrive om spesielt {@link #utvidSporingsdata(List, Collection, SporingsloggId)} og direkte kobling til konstant.er
 */
@Deprecated
public class AbacSporingslogg {

    private static final Logger SPORINGSLOGG = AppLoggerFactory.getSporingLogger(AbacSporingslogg.class);

    private static final char SPACE_SEPARATOR = ' ';
    private final String action;

    public AbacSporingslogg(String action) {
        this.action = action;
    }

    private static int kryssprodukt(Collection<?>... faktorer) {
        int resultat = 1;
        for (Collection<?> faktor : faktorer) {
            resultat *= Math.max(faktor.size(), 1);
        }
        return resultat;
    }

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

    public void loggDeny(PdpRequest pdpRequest, List<Decision> decisions, AbacAttributtSamling attributter) {
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < decisions.size(); i++) {
            if (decisions.get(i) == Decision.Deny) {
                pdpRequestSporingsdata.add(byggSporingsdata(pdpRequest, i, decisions.get(i)));
            }
        }

        List<Sporingsdata> sporingsdata = byggSammensattSporingsdata(attributter, pdpRequestSporingsdata, antallSporingsrader(attributter));
        logg(sporingsdata);
    }

    void loggTilgang(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        List<Sporingsdata> sporingsdata = byggSporingsdata(pdpRequest, attributter);
        logg(sporingsdata);
    }

    public List<Sporingsdata> byggSporingsdata(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        int antallRaderFraPdpRequest = antallResources(pdpRequest);
        List<Sporingsdata> pdpRequestSporingsdata = new ArrayList<>();
        for (int i = 0; i < antallRaderFraPdpRequest; i++) {
            pdpRequestSporingsdata.add(byggSporingsdata(pdpRequest, i));
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
        resultat.addAll(leggPåAttributter(Sporingsdata.opprett(), attributter, antallRaderFraAttributter));
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

        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getAksjonspunktKode(), StandardSporingsloggId.AKSJONSPUNKT_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getAktørIder(), StandardSporingsloggId.AKTOR_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getBehandlingsIder(), StandardSporingsloggId.BEHANDLING_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getBehandlingsUUIDer(), StandardSporingsloggId.BEHANDLING_UUID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getDokumentDataIDer(), StandardSporingsloggId.DOKUMENT_DATA_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getDokumentIDer(), StandardSporingsloggId.DOKUMENT_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getFagsakIder(), StandardSporingsloggId.FAGSAK_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getFnrForSøkEtterSaker(), StandardSporingsloggId.FNR_SOK);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getSaksnummre(), StandardSporingsloggId.GSAK_SAKSNUMMER);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getJournalpostIder(false), StandardSporingsloggId.JOURNALPOST_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getJournalpostIder(true), StandardSporingsloggId.JOURNALPOST_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getOppgaveIder(), StandardSporingsloggId.OPPGAVE_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getSPBeregningsIder(), StandardSporingsloggId.SPBEREGNING_ID);

        return sporingsdata;

    }

    private int antallSporingsrader(AbacAttributtSamling attributter) {
        return kryssprodukt(
            attributter.getAksjonspunktKode(),
            attributter.getAktørIder(),
            attributter.getBehandlingsIder(),
            attributter.getBehandlingsUUIDer(),
            attributter.getDokumentDataIDer(),
            attributter.getFagsakIder(),
            attributter.getFnrForSøkEtterSaker(),
            attributter.getJournalpostIder(false),
            attributter.getJournalpostIder(true),
            attributter.getOppgaveIder(),
            attributter.getSaksnummre(),
            attributter.getSPBeregningsIder());
    }

    private Sporingsdata byggSporingsdata(PdpRequest pdpRequest, int index, Decision decision) {
        return byggSporingsdata(pdpRequest, index).leggTilId(StandardSporingsloggId.ABAC_DECISION, decision.getEksternKode());
    }

    private Sporingsdata byggSporingsdata(PdpRequest pdpRequest, int index) {
        Sporingsdata sporingsdata = Sporingsdata.opprett()
            .leggTilId(StandardSporingsloggId.ABAC_ACTION, pdpRequest.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID))
            .leggTilId(StandardSporingsloggId.ABAC_RESOURCE_TYPE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        //hent ut fnr og aksjonpspunkt-typer vha indexer pga kryssprodukt mellom disse
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_FNR, index % Math.max(pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR), 1), StandardSporingsloggId.FNR);
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

    public void logg(List<Sporingsdata> sporingsdata) {
        for (Sporingsdata sporingsdatum : sporingsdata) {
            logg(sporingsdatum);
        }
    }

    private void logg(Sporingsdata sporingsdata) {
        StringBuilder msg = new StringBuilder()
            .append("action=").append(action).append(SPACE_SEPARATOR);
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

}
