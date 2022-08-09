package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.vedtak.sikkerhet.abac.AbacResultat.GODKJENT;
import static no.nav.vedtak.sikkerhet.abac.Decision.Deny;
import static no.nav.vedtak.sikkerhet.abac.Decision.Permit;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

@Default
@ApplicationScoped
public class PepImpl implements Pep {
    private final static String PIP = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker";

    private PdpKlient pdpKlient;
    private PdpRequestBuilder builder;

    private Set<String> pipUsers;
    private TokenProvider tokenProvider;
    private AbacAuditlogger auditlogger;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient,
            TokenProvider tokenProvider,
            PdpRequestBuilder pdpRequestBuilder,
            AbacAuditlogger auditlogger,
            @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdpKlient = pdpKlient;
        this.builder = pdpRequestBuilder;
        this.tokenProvider = tokenProvider;
        this.auditlogger = auditlogger;
        this.pipUsers = konfigurePipUsers(pipUsers);
    }

    protected Set<String> konfigurePipUsers(String pipUsers) {
        if (pipUsers != null) {
            return Set.of(pipUsers.toLowerCase().split(","));
        }
        return Set.of();
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter) {
        var pdpRequest = builder.lagPdpRequest(attributter);

        if (PIP.equals(attributter.getResource())) {
            return vurderTilgangTilPipTjeneste(pdpRequest, attributter);
        }
        return pdpKlient.forespørTilgang(pdpRequest);
    }

    protected Tilgangsbeslutning vurderTilgangTilPipTjeneste(PdpRequest pdpRequest, AbacAttributtSamling attributter) {
        String uid = tokenProvider.getUid();
        if (pipUsers.contains(uid.toLowerCase())) {
            return lagPipPermit(pdpRequest);
        }
        var tilgangsbeslutning = lagPipDeny(pdpRequest);
        auditlogger.loggDeny(uid, tilgangsbeslutning.getPdpRequest(), attributter);
        return tilgangsbeslutning;
    }

    protected Tilgangsbeslutning lagPipPermit(PdpRequest pdpRequest) {
        int antallResources = antallResources(pdpRequest);
        var decisions = lagDecisions(antallResources, Permit);
        return new Tilgangsbeslutning(GODKJENT, decisions, pdpRequest);
    }

    protected Tilgangsbeslutning lagPipDeny(PdpRequest pdpRequest) {
        int antallResources = antallResources(pdpRequest);
        var decisions = lagDecisions(antallResources, Deny);
        return new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, decisions, pdpRequest);
    }

    protected int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, getAntallResources(pdpRequest));
    }

    protected int antallIdenter(PdpRequest pdpRequest) {
        // antall identer involvert i en request (eks. default - antall aktørId + antall
        // fnr)
        return pdpRequest.getAntall(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)
                + pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
    }

    protected int getAntallResources(@SuppressWarnings("unused") PdpRequest pdpRequest) {
        // Template method. Regn evt ut antall aksjonspunkter el andre typer ressurser
        // som behandles i denne requesten (hvis mer enn 1)
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
