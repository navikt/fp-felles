package no.nav.foreldrepenger.sikkerhet.abac.pdp2;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.Pdp;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Advice;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponse;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
@Named("nyPdp")
public class NyPdpImpl implements Pdp {

    private static final Logger LOG = LoggerFactory.getLogger(NyPdpImpl.class);

    private static final String POLICY_IDENTIFIER = "no.nav.abac.attributter.adviceorobligation.deny_policy";
    private static final String DENY_ADVICE_IDENTIFIER = "no.nav.abac.advices.reason.deny_reason";

    private final NyXacmlConsumer pdpKlient;

    @Inject
    public NyPdpImpl(NyXacmlConsumer pdpKlient) {
        this.pdpKlient = pdpKlient;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        XacmlResponse xacmlResponse = pdpKlient.evaluate(NyXacmlRequestMapper.lagXacmlRequest(pdpRequest));

        var decisions = collectDecisions(xacmlResponse);
        validerObligations(xacmlResponse.getResponse());
        var hovedresultat = lagResultat(decisions, xacmlResponse.getResponse());
        return new Tilgangsbeslutning(hovedresultat, decisions, pdpRequest);
    }

    private List<Decision> collectDecisions(final XacmlResponse nyResponse) {
        var decisions = nyResponse.getResponse().stream().map(XacmlResponse.Response::getDecision).collect(Collectors.toList());
        valider(decisions);
        return decisions;
    }

    private static AbacResultat lagResultat(List<Decision> decisions, List<XacmlResponse.Response> response) {
        if (aggregatedDecision(decisions) == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }

        var denyAdvices = response.stream()
            .map(XacmlResponse.Response::getAssociatedAdvice)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(advice -> advice.getId().equals(DENY_ADVICE_IDENTIFIER))
            .flatMap(advice -> advice.getAttributeAssignment().stream()
                .filter(attributeAssignment -> attributeAssignment.getAttributeId().equals(POLICY_IDENTIFIER))
                .map(denyPolicy -> mapToAdvice(denyPolicy.getValue()))
                .filter(Objects::nonNull))
            .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: " + toStringWithoutLineBreaks(denyAdvices));
        }

        if (denyAdvices.contains(Advice.DENY_KODE_6)) {
            return AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvices.contains(Advice.DENY_KODE_7)) {
            return AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvices.contains(Advice.DENY_EGEN_ANSATT)) {
            return AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static Advice mapToAdvice(String adviceString) {
        switch (adviceString) {
            case "fp3_behandle_egen_ansatt": return Advice.DENY_EGEN_ANSATT;
            case "fp2_behandle_kode7": return Advice.DENY_KODE_7;
            case "fp1_behandle_kode6": return Advice.DENY_KODE_6;
            default: return null;
        }
    }

    private static Decision aggregatedDecision(List<Decision> decisions) {
        if (decisions.stream().allMatch(dec -> dec.equals(Decision.Permit))) {
            return Decision.Permit;
        }
        return Decision.Deny;
    }

    private static void valider(final List<Decision> decisions) {
        if (decisions.stream().anyMatch(dec -> dec.equals(Decision.Indeterminate))) {
            throw new TekniskException("F-080281", "Decision.Indeterminate fra PDP, dette skal aldri skje.");
        }
    }

    private static void validerObligations(List<XacmlResponse.Response> response) {
        var obligations = response.stream()
            .map(XacmlResponse.Response::getObligations)
            .filter(Objects::nonNull)
            .flatMap(List::stream).collect(Collectors.toList());
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027",
                String.format("Mottok ukjente obligations fra PDP: %s",
                    obligations.stream()
                        .map(XacmlResponse.Assignments::getAttributeAssignment)
                        .filter(Objects::nonNull)
                        .flatMap(obligation -> obligation.stream()
                            .filter(Objects::nonNull)
                            .map(XacmlResponse.AttributeAssignment::getValue))
                        .collect(Collectors.joining(", "))
                ));
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
            .map(NyPdpImpl::removeLineBreaks)
            .orElse(null);
    }
}
