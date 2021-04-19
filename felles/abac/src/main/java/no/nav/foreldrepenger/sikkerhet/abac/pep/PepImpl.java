package no.nav.foreldrepenger.sikkerhet.abac.pep;

import static no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat.GODKJENT;
import static no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision.Deny;
import static no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision.Permit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.sikkerhet.abac.PdpRequestBuilder;
import no.nav.foreldrepenger.sikkerhet.abac.auditlog.AbacAuditlogger;
import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.Pdp;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.vedtak.konfig.KonfigVerdi;

@Default
@ApplicationScoped
public class PepImpl implements Pep {
    private static final Logger LOG = LoggerFactory.getLogger(PepImpl.class);

    private final static String PIP = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker";

    private final Pdp pdp;
    private final Pdp nyPdp;
    private final PdpRequestBuilder builder;
    private final Set<String> pipUsers;
    private final AbacAuditlogger auditlogger;

    @Inject
    public PepImpl(@Named("oldPdp") Pdp pdp,
                   @Named("nyPdp") Pdp nyPdp,
                   PdpRequestBuilder pdpRequestBuilder,
                   AbacAuditlogger auditlogger,
                   @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdp = pdp;
        this.nyPdp = nyPdp;
        this.builder = pdpRequestBuilder;
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
    public Tilgangsbeslutning vurderTilgang(BeskyttRessursAttributer ressursAttributer) {
        var pdpRequest = builder.lagPdpRequest(ressursAttributer);
        if (PIP.equals(ressursAttributer.getResource())) {
            return vurderTilgangTilPipTjenesten(pdpRequest, ressursAttributer);
        }

        var tilgangsbeslutning = pdp.forespørTilgang(pdpRequest);

        Tilgangsbeslutning nytilgangsbeslutning;
        try {
            nytilgangsbeslutning = nyPdp.forespørTilgang(pdpRequest);
            sammenlignResultat(tilgangsbeslutning, nytilgangsbeslutning);
        } catch (Exception e) {
            LOG.info("Fikk exception fra ny pdp tjeneste.", e);
        }
        return tilgangsbeslutning;
    }

    private void sammenlignResultat(final Tilgangsbeslutning tilgangsbeslutning, final Tilgangsbeslutning nytilgangsbeslutning) {
        if (tilgangsbeslutning != null && nytilgangsbeslutning != null) {
            if (!tilgangsbeslutning.getBeslutningKode().equals(nytilgangsbeslutning.getBeslutningKode())) {
                LOG.info("PEP: Fikk forskjellig tilgangsbeslutning old:{} vs new:{}", tilgangsbeslutning.getBeslutningKode(), nytilgangsbeslutning.getBeslutningKode());
            }
            if (tilgangsbeslutning.getDelbeslutninger().size() != nytilgangsbeslutning.getDelbeslutninger().size()) {
                LOG.info("PEP: Antall delbeslutninger er forskjellig old:{} vs new:{}", tilgangsbeslutning.getDelbeslutninger(), nytilgangsbeslutning.getDelbeslutninger());
            }
        } else {
            LOG.info("Resultatene kan ikke være null, old:{} vs new:{}", tilgangsbeslutning, nytilgangsbeslutning);
        }
    }

    protected Tilgangsbeslutning vurderTilgangTilPipTjenesten(PdpRequest pdpRequest, BeskyttRessursAttributer ressursAttributer) {
        String uid = pdpRequest.getUserId();
        if (pipUsers.contains(uid.toLowerCase())) {
            return lagBeslutning(pdpRequest, Permit);
        }
        var tilgangsbeslutning = lagBeslutning(pdpRequest, Deny);
        auditlogger.loggDeny(tilgangsbeslutning.getPdpRequest(), ressursAttributer);
        return tilgangsbeslutning;
    }

    protected Tilgangsbeslutning lagBeslutning(PdpRequest pdpRequest, Decision decision) {
        int antallResources = antallResources(pdpRequest);
        var decisions = lagDecisions(antallResources, decision);
        return new Tilgangsbeslutning(decision.equals(Permit) ? GODKJENT : AVSLÅTT_ANNEN_ÅRSAK, decisions, pdpRequest);
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1,  pdpRequest.getAksjonspunkter().size());
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAktørIder().size()
                + pdpRequest.getPersonnummere().size();
    }

    private List<Decision> lagDecisions(int antallDecisions, Decision decision) {
        List<Decision> decisions = new ArrayList<>();
        for (int i = 0; i < antallDecisions; i++) {
            decisions.add(decision);
        }
        return decisions;
    }

}
