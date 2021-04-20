package no.nav.foreldrepenger.sikkerhet.abac.pdp.xacml;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Advice;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;

public class XacmlResponseWrapper {

    public static final String ATTRIBUTE_ASSIGNMENT = "AttributeAssignment";
    private static final String RESPONSE = "Response";
    private static final String DECISION = "Decision";
    private static final String OBLIGATIONS = "Obligations";
    private static final String ADVICE = "AssociatedAdvice";
    private static final String POLICY_IDENTIFIER = "no.nav.abac.attributter.adviceorobligation.deny_policy";
    private static final String DENY_ADVICE_IDENTIFIER = "no.nav.abac.advices.reason.deny_reason";

    private JsonObject responseJson;

    public XacmlResponseWrapper(JsonObject response) {
        this.responseJson = response;
    }

    public List<Obligation> getObligations() {
        JsonValue v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                .map(this::getObligationsFromObject)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        } else {
            return getObligationsFromObject(responseJson.getJsonObject(RESPONSE));
        }
    }

    private List<Obligation> getObligationsFromObject(JsonValue jsonValue) {
        if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
            return jsonValue.asJsonArray()
                .stream()
                .map(this::getObligationsFromObject)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        } else {
            return getObligationsFromObject(jsonValue.asJsonObject());
        }
    }

    private List<Obligation> getObligationsFromObject(JsonObject jsonObject) {
        if (jsonObject.containsKey(OBLIGATIONS)) {
            if (jsonObject.get(OBLIGATIONS).getValueType() == JsonValue.ValueType.ARRAY) {
                JsonArray jsonArray = jsonObject.getJsonArray(OBLIGATIONS);
                return jsonArray.stream()
                    .map(jsonValue -> new Obligation((JsonObject) jsonValue))
                    .collect(Collectors.toList());
            } else {
                Obligation obligation = new Obligation(jsonObject.getJsonObject(OBLIGATIONS));
                return Collections.singletonList(obligation);
            }
        }
        return Collections.emptyList();
    }

    public List<Advice> getAdvice() {
        JsonValue v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                .map(this::getAdviceFromObject)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        } else {
            return getAdvicefromObject(responseJson.getJsonObject(RESPONSE));
        }
    }

    private List<Advice> getAdviceFromObject(JsonValue jsonValue) {
        if (jsonValue.getValueType() == JsonValue.ValueType.ARRAY) {
            return jsonValue.asJsonArray()
                .stream()
                .map(this::getAdviceFromObject)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        } else {
            return getAdvicefromObject(jsonValue.asJsonObject());
        }
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
        } else {
            JsonObject adviceObject = objectValue.asJsonObject();
            if (!DENY_ADVICE_IDENTIFIER.equals(adviceObject.getString("Id"))) {
                return Collections.emptyList();
            }
            return getAdvice(adviceObject);
        }
    }

    private List<Advice> getAdvice(JsonObject responseObject) {
        if (responseObject.get(ATTRIBUTE_ASSIGNMENT).getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray adviceArray = responseObject.getJsonArray(ATTRIBUTE_ASSIGNMENT);
            return adviceArray.stream()
                .map(jsonValue -> jsonToAdvice((JsonObject) jsonValue))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } else {
            Optional<Advice> advice = jsonToAdvice(responseObject.getJsonObject(ATTRIBUTE_ASSIGNMENT));
            return advice.map(Collections::singletonList).orElse(Collections.emptyList());
        }
    }

    private Optional<Advice> jsonToAdvice(JsonObject advice) {
        String attributeId = advice.getString("AttributeId");
        String attributeValue = advice.getString("Value");

        if (!POLICY_IDENTIFIER.equals(attributeId)) {
            return Optional.empty();
        }
        switch (attributeValue) {
            case "fp3_behandle_egen_ansatt":
                return Optional.of(Advice.DENY_EGEN_ANSATT);
            case "fp2_behandle_kode7":
                return Optional.of(Advice.DENY_KODE_7);
            case "fp1_behandle_kode6":
                return Optional.of(Advice.DENY_KODE_6);
            default:
                return Optional.empty();
        }
    }

    public List<Decision> getDecisions() {
        JsonValue response = responseJson.get(RESPONSE);
        if (response.getValueType() == JsonValue.ValueType.ARRAY) {
            return responseJson.getJsonArray(RESPONSE).stream()
                .map(jsonValue -> ((JsonObject) jsonValue).getString(DECISION))
                .map(Decision::valueOf)
                .collect(Collectors.toList());

        } else {
            return Collections.singletonList(Decision.valueOf(responseJson.getJsonObject(RESPONSE).getString(DECISION)));
        }
    }

    @Override
    public String toString() {
        return "XacmlResponse(" + responseJson + ")";
    }

}
