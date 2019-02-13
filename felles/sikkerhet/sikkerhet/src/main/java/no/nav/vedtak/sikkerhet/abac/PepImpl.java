package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Dependent
public class PepImpl implements Pep {

    private PdpKlient pdpKlient;
    private PdpRequestBuilder pdpRequestBuilder;

    private String PDP_USER_ID = "srvpdp";

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient, PdpRequestBuilder pdpRequestBuilder) {
        this.pdpKlient = pdpKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter) {
        validerInput(attributter);
        PdpRequest pdpRequest = pdpRequestBuilder.lagPdpRequest(attributter);

        if (BeskyttetRessursResourceAttributt.PIP.equals(attributter.getResource())) {
            return vurderTilgangTilPipTjeneste(pdpRequest, attributter);
        } else {
            return pdpKlient.forespørTilgang(pdpRequest);
        }
    }

    private Tilgangsbeslutning vurderTilgangTilPipTjeneste(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        String uid = SubjectHandler.getSubjectHandler().getUid();
        if (PDP_USER_ID.equals(uid.toLowerCase())) {
            return lagPipPermit(pdpRequest);
        }
        Tilgangsbeslutning tilgangsbeslutning = lagPipDeny(pdpRequest);
        AbacSporingslogg sporingslogg = new AbacSporingslogg(attributter.getAction());
        sporingslogg.loggDeny(pdpRequest, tilgangsbeslutning.getDelbeslutninger(), attributter);
        return tilgangsbeslutning;
    }

    private Tilgangsbeslutning lagPipPermit(PdpRequest pdpRequest) {
        List<Decision> decisions = lagDecisions(pdpRequest.antallResources(), Decision.Permit);
        return new Tilgangsbeslutning(AbacResultat.GODKJENT, decisions, pdpRequest);
    }

    private Tilgangsbeslutning lagPipDeny(PdpRequest pdpRequest) {
        List<Decision> decisions = lagDecisions(pdpRequest.antallResources(), Decision.Deny);
        return new Tilgangsbeslutning(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK, decisions, pdpRequest);
    }

    private List<Decision> lagDecisions(int antallDecisions, Decision decision) {
        List<Decision> decisions = new ArrayList<>();
        for (int i = 0; i < antallDecisions; i++) {
            decisions.add(decision);
        }
        return decisions;
    }

    private void validerInput(AbacAttributtSamling attributter) {
        if (attributter.getBehandlingsIder().size() > 1) {
            throw PepFeil.FACTORY.ugyldigInputForMangeBehandlingIder(attributter.getBehandlingsIder()).toException();
        }
    }


}