package no.nav.vedtak.sikkerhet.abac;


import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import no.nav.abac.xacml.NavAttributter;
import no.nav.abac.xacml.StandardAttributter;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggId;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

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
        return pdpRequest.getAntall(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
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

        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getAksjonspunktKode(), SporingsloggId.AKSJONSPUNKT_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getAktørIder(), SporingsloggId.AKTOR_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getBehandlingsIder(), SporingsloggId.BEHANDLING_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getBehandlingsUUIDer(), SporingsloggId.BEHANDLING_UUID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getDokumentDataIDer(), SporingsloggId.DOKUMENT_DATA_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getDokumentIDer(), SporingsloggId.DOKUMENT_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getFagsakIder(), SporingsloggId.FAGSAK_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getFnrForSøkEtterSaker(), SporingsloggId.FNR_SOK);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getSaksnummre(), SporingsloggId.GSAK_SAKSNUMMER);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getJournalpostIder(false), SporingsloggId.JOURNALPOST_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getJournalpostIder(true), SporingsloggId.JOURNALPOST_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getOppgaveIder(), SporingsloggId.OPPGAVE_ID);
        sporingsdata = utvidSporingsdata(sporingsdata, attributter.getSPBeregningsIder(), SporingsloggId.SPBEREGNING_ID);

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
        return byggSporingsdata(pdpRequest, index).leggTilId(SporingsloggId.ABAC_DECISION, decision.getEksternKode());
    }

    private Sporingsdata byggSporingsdata(PdpRequest pdpRequest, int index) {
        Sporingsdata sporingsdata = Sporingsdata.opprett()
            .leggTilId(SporingsloggId.ABAC_ACTION, pdpRequest.getString(StandardAttributter.ACTION_ID))
            .leggTilId(SporingsloggId.ABAC_RESOURCE_TYPE, pdpRequest.getString(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE));

        //hent ut fnr og aksjonpspunkt-typer vha indexer pga kryssprodukt mellom disse
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, RESOURCE_FELLES_PERSON_FNR, index % Math.max(pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR), 1), SporingsloggId.FNR);
        setOptionalListValueinAttributeSet(sporingsdata, pdpRequest, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, (index / Math.max(antallIdenter(pdpRequest), 1)), SporingsloggId.ABAC_AKSJONSPUNKT_TYPE);

        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, NavAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, SporingsloggId.ABAC_ANSVALIG_SAKSBEHANDLER);
        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, NavAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, SporingsloggId.ABAC_BEHANDLING_STATUS);
        setOptionalValueinAttributeSet(sporingsdata, pdpRequest, NavAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, SporingsloggId.ABAC_SAK_STATUS);
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
            String nøkkel = id.getEksternKode();
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
