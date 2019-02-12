package no.nav.vedtak.sikkerhet.pdp.xacml;

import java.util.Objects;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class XacmlAttributeSet {
    private static final String VALUE = "Value";
    private static final String ATTRIBUTEID = "AttributeId";
    private static final String ATTRIBUTE = "Attribute";

    private JsonArrayBuilder attributes = Json.createArrayBuilder();

    public XacmlAttributeSet addAttribute(String id, String value) {
        Objects.requireNonNull(id != null, "Name in JsonObject's name/value pair");
        Objects.requireNonNull(value != null, "Value in JsonObject's name/value pair");
        attributes.add(createAttribute(id, value));
        return this;
    }

    private JsonObjectBuilder createAttribute(String id, String value) {
        return Json.createObjectBuilder().add(ATTRIBUTEID, id).add(VALUE, value);
    }

    JsonObjectBuilder getAttributes() {
        return Json.createObjectBuilder().add(ATTRIBUTE, attributes);
    }
}


