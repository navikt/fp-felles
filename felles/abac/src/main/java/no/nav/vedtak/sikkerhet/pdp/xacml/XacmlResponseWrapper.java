package no.nav.vedtak.sikkerhet.pdp.xacml;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonValue;

import no.nav.vedtak.sikkerhet.abac.Decision;

public class XacmlResponseWrapper {

    public static final String ATTRIBUTE_ASSIGNMENT = "AttributeAssignment";
    private static final String RESPONSE = "Response";
    private static final String DECISION = "Decision";
    private static final String OBLIGATIONS = "Obligations";
    private static final String ADVICE = "AssociatedAdvice";
    private static final String POLICY_IDENTIFIER = "no.nav.abac.attributter.adviceorobligation.deny_policy";
    private static final String DENY_ADVICE_IDENTIFIER = "no.nav.abac.advices.reason.deny_reason";

    private final JsonObject responseJson;

    public XacmlResponseWrapper(JsonObject response) {
        this.responseJson = response;
    }

    public List<Obligation> getObligations() {
        var v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            var jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                    .map(this::getObligationsFromObject)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
        return getObligationsFromObject(responseJson.getJsonObject(RESPONSE));
    }

    private List<Obligation> getObligationsFromObject(JsonValue jsonValue) {
        if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
            return jsonValue.asJsonArray()
                    .stream()
                    .map(this::getObligationsFromObject)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return getObligationsFromObject(jsonValue.asJsonObject());
    }

    private List<Obligation> getObligationsFromObject(JsonObject jsonObject) {
        if (jsonObject.containsKey(OBLIGATIONS)) {
            if (jsonObject.get(OBLIGATIONS).getValueType() == JsonValue.ValueType.ARRAY) {
                var jsonArray = jsonObject.getJsonArray(OBLIGATIONS);
                return jsonArray.stream()
                        .map(jsonValue -> new Obligation((JsonObject) jsonValue))
                        .collect(Collectors.toList());
            }
            var obligation = new Obligation(jsonObject.getJsonObject(OBLIGATIONS));
            return List.of(obligation);
        }
        return List.of();
    }

    public List<Advice> getAdvice() {
        var v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            var jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                    .map(this::getAdviceFromObject)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
        return getAdvicefromObject(responseJson.getJsonObject(RESPONSE));
    }

    private List<Advice> getAdviceFromObject(JsonValue jsonValue) {
        if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
            return jsonValue.asJsonArray()
                    .stream()
                    .map(this::getAdviceFromObject)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return getAdvicefromObject(jsonValue.asJsonObject());
    }

    private List<Advice> getAdvicefromObject(JsonObject responseObject) {
        if (!responseObject.containsKey(ADVICE)) {
            return Collections.emptyList();
        }
        var objectValue = responseObject.get(ADVICE);
        if (objectValue.getValueType() == JsonValue.ValueType.ARRAY) {
            return objectValue.asJsonArray().stream()
                    .map(JsonValue::asJsonObject)
                    .map(this::getAdvice)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        var adviceObject = objectValue.asJsonObject();
        if (!DENY_ADVICE_IDENTIFIER.equals(adviceObject.getString("Id"))) {
            return List.of();
        }
        return getAdvice(adviceObject);
    }

    private List<Advice> getAdvice(JsonObject responseObject) {
        if (responseObject.get(ATTRIBUTE_ASSIGNMENT).getValueType() == JsonValue.ValueType.ARRAY) {
            var adviceArray = responseObject.getJsonArray(ATTRIBUTE_ASSIGNMENT);
            return adviceArray.stream()
                    .map(jsonValue -> jsonToAdvice((JsonObject) jsonValue))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
        var advice = jsonToAdvice(responseObject.getJsonObject(ATTRIBUTE_ASSIGNMENT));
        return advice.map(Collections::singletonList).orElse(List.of());
    }

    private Optional<Advice> jsonToAdvice(JsonObject advice) {
        var attributeId = advice.getString("AttributeId");
        var attributeValue = advice.getString("Value");

        if (!POLICY_IDENTIFIER.equals(attributeId)) {
            return Optional.empty();
        }
        return switch (attributeValue) {
            case "fp3_behandle_egen_ansatt" -> Optional.of(Advice.DENY_EGEN_ANSATT);
            case "fp2_behandle_kode7" -> Optional.of(Advice.DENY_KODE_7);
            case "fp1_behandle_kode6" -> Optional.of(Advice.DENY_KODE_6);
            default -> Optional.empty();
        };
    }

    public List<Decision> getDecisions() {
        var response = responseJson.get(RESPONSE);
        if (response.getValueType() == JsonValue.ValueType.ARRAY) {
            return responseJson.getJsonArray(RESPONSE).stream()
                    .map(jsonValue -> ((JsonObject) jsonValue).getString(DECISION))
                    .map(Decision::valueOf)
                    .collect(Collectors.toList());

        }
        return List.of(Decision.valueOf(responseJson.getJsonObject(RESPONSE).getString(DECISION)));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [responseJson=" + responseJson + "]";
    }
}
