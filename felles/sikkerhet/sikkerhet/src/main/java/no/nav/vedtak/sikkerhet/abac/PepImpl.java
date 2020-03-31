package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Default
@ApplicationScoped
public class PepImpl implements Pep {
    private final static String PIP = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker";


    private PdpKlient pdpKlient;
    private PdpRequestBuilder pdpRequestBuilder;

    private Set<String> pipUsers;
    private AbacSporingslogg sporingslogg;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient,
                   PdpRequestBuilder pdpRequestBuilder,
                   AbacSporingslogg sporingslogg,
                   @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdpKlient = pdpKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;
        this.sporingslogg = sporingslogg;

        this.pipUsers = konfigurePipUsers(pipUsers);
    }

    protected Set<String> konfigurePipUsers(String pipUsers) {
        Set<String> result = new HashSet<>();
        if (pipUsers != null) {
            Collections.addAll(result, pipUsers.toLowerCase().split(","));
        }
        return result;
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = pdpRequestBuilder.lagPdpRequest(attributter);

        if (PIP.equals(attributter.getResource())) {
            return vurderTilgangTilPipTjeneste(pdpRequest, attributter);
        } else {
            return pdpKlient.forespørTilgang(pdpRequest);
        }
    }

    protected Tilgangsbeslutning vurderTilgangTilPipTjeneste(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        String uid = SubjectHandler.getSubjectHandler().getUid();
        if (pipUsers.contains(uid.toLowerCase())) {
            return lagPipPermit(pdpRequest);
        }
        Tilgangsbeslutning tilgangsbeslutning = lagPipDeny(pdpRequest);
        sporingslogg.loggDeny(tilgangsbeslutning, attributter);
        return tilgangsbeslutning;
    }

    protected Tilgangsbeslutning lagPipPermit(PdpRequest pdpRequest) {
        int antallResources = antallResources(pdpRequest);
        List<Decision> decisions = lagDecisions(antallResources, Decision.Permit);
        return new Tilgangsbeslutning(AbacResultat.GODKJENT, decisions, pdpRequest);
    }

    protected Tilgangsbeslutning lagPipDeny(PdpRequest pdpRequest) {
        int antallResources = antallResources(pdpRequest);
        List<Decision> decisions = lagDecisions(antallResources, Decision.Deny);
        return new Tilgangsbeslutning(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK, decisions, pdpRequest);
    }

    protected int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, getAntallResources(pdpRequest));
    }

    protected int antallIdenter(PdpRequest pdpRequest) {
        // antall identer involvert i en request (eks. default - antall aktørId + antall fnr)
        return pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)
            + pdpRequest.getAntall(no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }

    protected int getAntallResources(PdpRequest pdpRequest) {
        // Template method. Regn evt ut antall aksjonspunkter el andre typer ressurser som behandles i denne requesten (hvis mer enn 1)
        return 1;
    }

    private List<Decision> lagDecisions(int antallDecisions, Decision decision) {
        List<Decision> decisions = new ArrayList<>();
        for (int i = 0; i < antallDecisions; i++) {
            decisions.add(decision);
        }
        return decisions;
    }

}
