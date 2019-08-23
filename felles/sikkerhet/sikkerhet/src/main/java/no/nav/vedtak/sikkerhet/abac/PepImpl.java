package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.abac.xacml.CommonAttributter;
import no.nav.abac.xacml.ForeldrepengerAttributter;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class PepImpl implements Pep {

    private PdpKlient pdpKlient;
    private PdpRequestBuilder pdpRequestBuilder;

    private Set<String> pipUsers;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient, PdpRequestBuilder pdpRequestBuilder, @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdpKlient = pdpKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;

        this.pipUsers = konfigurePipUsers(pipUsers);
    }

    private Set<String> konfigurePipUsers(String pipUsers) {
        Set<String> result = new HashSet<>();
        if (pipUsers != null) {
            Collections.addAll(result, pipUsers.toLowerCase().split(","));
        }
        return result;
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
        if (pipUsers.contains(uid.toLowerCase())) {
            return lagPipPermit(pdpRequest);
        }
        Tilgangsbeslutning tilgangsbeslutning = lagPipDeny(pdpRequest);
        AbacSporingslogg sporingslogg = new AbacSporingslogg(attributter.getAction());
        sporingslogg.loggDeny(pdpRequest, tilgangsbeslutning.getDelbeslutninger(), attributter);
        return tilgangsbeslutning;
    }

    private Tilgangsbeslutning lagPipPermit(PdpRequest pdpRequest) {
        List<Decision> decisions = lagDecisions(antallResources(pdpRequest), Decision.Permit);
        return new Tilgangsbeslutning(AbacResultat.GODKJENT, decisions, pdpRequest);
    }

    private Tilgangsbeslutning lagPipDeny(PdpRequest pdpRequest) {
        List<Decision> decisions = lagDecisions(antallResources(pdpRequest), Decision.Deny);
        return new Tilgangsbeslutning(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK, decisions, pdpRequest);
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
