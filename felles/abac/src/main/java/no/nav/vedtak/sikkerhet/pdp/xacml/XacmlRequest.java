package no.nav.vedtak.sikkerhet.pdp.xacml;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record XacmlRequest(
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("Request") Map<Category, List<Attributes>> request) {

    public static record Attributes(
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) @JsonProperty("Attribute") List<AttributeAssignment> attribute) {
    }

    public static record AttributeAssignment(@JsonProperty("AttributeId") String attributeId, @JsonProperty("Value") Object value) {
    }
}
