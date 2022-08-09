package no.nav.vedtak.sikkerhet.pdp2.xacml;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.vedtak.sikkerhet.abac.Decision;

public final class XacmlResponseMapper {

    private static final String POLICY_IDENTIFIER = "no.nav.abac.attributter.adviceorobligation.deny_policy";
    private static final String DENY_ADVICE_IDENTIFIER = "no.nav.abac.advices.reason.deny_reason";

    public static List<XacmlResponse.ObligationOrAdvice> getObligations(XacmlResponse response) {
        return Optional.ofNullable(response)
            .map(XacmlResponse::response).orElse(List.of()).stream()
            .map(r -> Optional.ofNullable(r.obligations()).orElse(List.of()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public static List<Advice> getAdvice(XacmlResponse response) {
        return Optional.ofNullable(response)
            .map(XacmlResponse::response).orElse(List.of()).stream()
            .map(r -> Optional.ofNullable(r.associatedAdvice()).orElse(List.of()))
            .flatMap(Collection::stream)
            .map(XacmlResponseMapper::getAdviceFrom)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    private static List<Advice> getAdviceFrom(XacmlResponse.ObligationOrAdvice advice) {
        if (!DENY_ADVICE_IDENTIFIER.equals(advice.id())) {
            return List.of();
        }
        var denials = advice.attributeAssignment().stream()
            .map(a -> getAdvicefromObject(a))
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

        return denials;
    }

    private static Optional<Advice> getAdvicefromObject(XacmlResponse.AttributeAssignment attribute) {
        var attributeId = attribute.attributeId();

        if (!POLICY_IDENTIFIER.equals(attributeId)) {
            return Optional.empty();
        }
        var attributeValue = (String)attribute.value();
        return switch (attributeValue) {
            case "fp3_behandle_egen_ansatt" -> Optional.of(Advice.DENY_EGEN_ANSATT);
            case "fp2_behandle_kode7" -> Optional.of(Advice.DENY_KODE_7);
            case "fp1_behandle_kode6" -> Optional.of(Advice.DENY_KODE_6);
            default -> Optional.empty();
        };
    }

    public static List<Decision> getDecisions(XacmlResponse response) {
        return response.response().stream()
            .map(XacmlResponse.Result::decision)
            .map(Decision::valueOf)
            .collect(Collectors.toList());
    }
}
