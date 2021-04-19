package no.nav.foreldrepenger.sikkerhet.abac.pdp;


import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Advice;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.BiasedDecisionResponse;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponseWrapper;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
@Named("oldPdp")
public class PdpImpl implements Pdp {

    private static final Logger LOG = LoggerFactory.getLogger(PdpImpl.class);

    private final XacmlConsumer pdpKlient;

    @Inject
    public PdpImpl(XacmlConsumer pdpKlient) {
        this.pdpKlient = pdpKlient;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        var builder = XacmlRequestMapper.lagXacmlRequestBuilder(pdpRequest);
        var response = pdpKlient.evaluate(builder);
        var biasedDecisionResponse = evaluateWithBias(response);
        var hovedresultat = resultatFraResponse(biasedDecisionResponse);
        return new Tilgangsbeslutning(hovedresultat, response.getDecisions(), pdpRequest);
    }

    private static AbacResultat resultatFraResponse(BiasedDecisionResponse response) {
        if (response.getBiasedDecision() == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }
        var denyAdvice = response.getXacmlResponse().getAdvice();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: " + toStringWithoutLineBreaks(denyAdvice));
        }
        if (denyAdvice.contains(Advice.DENY_KODE_6)) {
            return AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvice.contains(Advice.DENY_KODE_7)) {
            return AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvice.contains(Advice.DENY_EGEN_ANSATT)) {
            return AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static BiasedDecisionResponse evaluateWithBias(XacmlResponseWrapper response) {
        var decisions = response.getDecisions();
        validerDecisions(decisions);
        var biasedDecision = createAggregatedDecision(decisions);
        var decisionResponse = new BiasedDecisionResponse(biasedDecision, response);
        harObligations(decisionResponse);
        return decisionResponse;
    }

    private static void validerDecisions(final List<Decision> decisions) {
        if (decisions.stream().anyMatch(dec -> dec.equals(Decision.Indeterminate))) {
            throw new TekniskException("F-080281", "Decision.Indeterminate fra PDP, dette skal aldri skje.");
        }
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        if (decisions.stream().allMatch(dec -> dec.equals(Decision.Permit))) {
            return Decision.Permit;
        }
        return Decision.Deny;
    }

    private static void harObligations(BiasedDecisionResponse response) {
        var obligations = response.getXacmlResponse().getObligations();
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }

    private static String removeLineBreaks(String string) {
        return Optional.ofNullable(string)
            .map(s -> s.replaceAll("(\\r|\\n)", ""))
            .orElse(null);
    }

    private static String toStringWithoutLineBreaks(Object object) {
        return Optional.ofNullable(object)
            .map(Object::toString)
            .map(PdpImpl::removeLineBreaks)
            .orElse(null);
    }
}
