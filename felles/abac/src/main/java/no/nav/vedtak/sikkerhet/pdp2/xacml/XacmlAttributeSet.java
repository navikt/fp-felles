package no.nav.vedtak.sikkerhet.pdp2.xacml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XacmlAttributeSet {
    private List<XacmlRequest.AttributeAssignment> attributes = new ArrayList<>();

    public XacmlAttributeSet addAttribute(String id, String value) {
        Objects.requireNonNull(id, "Name in JsonObject's name/value pair");
        Objects.requireNonNull(value, "Value in JsonObject's name/value pair");
        attributes.add(new XacmlRequest.AttributeAssignment(id, value));
        return this;
    }

    public XacmlAttributeSet addAttribute(String id, int value) {
        Objects.requireNonNull(id, "Name in JsonObject's name/value pair");
        Objects.requireNonNull(value, "Value in JsonObject's name/value pair");
        attributes.add(new XacmlRequest.AttributeAssignment(id, value));
        return this;
    }

    List<XacmlRequest.AttributeAssignment> getAttributes() {
        return attributes;
    }
}
