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
import no.nav.foreldrepenger.sikkerhet.abac.pdp.XacmlConsumer;
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
        validerObligations(xacmlResponse.Response());
        var hovedresultat = lagResultat(decisions, xacmlResponse.Response());
        return new Tilgangsbeslutning(hovedresultat, decisions, pdpRequest);
    }

    private List<Decision> collectDecisions(final XacmlResponse nyResponse) {
        var decisions = nyResponse.Response().stream().map(XacmlResponse.Response::Decision).collect(Collectors.toList());
        valider(decisions);
        return decisions;
    }

    private static AbacResultat lagResultat(List<Decision> decisions, List<XacmlResponse.Response> response) {
        if (aggregatedDecision(decisions) == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }

        var denyAdvices = response.stream()
            .map(XacmlResponse.Response::AssociatedAdvice)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(advice -> advice.Id().equals(DENY_ADVICE_IDENTIFIER))
            .flatMap(advice -> advice.AttributeAssignment().stream()
                .filter(attributeAssignment -> attributeAssignment.AttributeId().equals(POLICY_IDENTIFIER))
                .map(denyPolicy -> mapToAdvice(denyPolicy.Value()))
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
        return switch (adviceString) {
            case "fp3_behandle_egen_ansatt" -> Advice.DENY_EGEN_ANSATT;
            case "fp2_behandle_kode7" -> Advice.DENY_KODE_7;
            case "fp1_behandle_kode6" -> Advice.DENY_KODE_6;
            default -> null;
        };
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
            .map(XacmlResponse.Response::Obligations)
            .filter(Objects::nonNull)
            .flatMap(List::stream).collect(Collectors.toList());
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027",
                String.format("Mottok ukjente obligations fra PDP: %s",
                    obligations.stream()
                        .map(XacmlResponse.Assignments::AttributeAssignment)
                        .filter(Objects::nonNull)
                        .flatMap(obligation -> obligation.stream()
                            .filter(Objects::nonNull)
                            .map(XacmlResponse.AttributeAssignment::Value))
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
