package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class XacmlResponse {

    @JsonProperty("Response")
    private List<Response> response;

    public XacmlResponse() {
    }

    public XacmlResponse(final List<Response> Response) {
        this.response = Response;
    }

    public List<Response> getResponse() {
        return response;
    }

    public void setResponse(final List<XacmlResponse.Response> response) {
        this.response = response;
    }

    public static class Response {

        @JsonProperty("Decision")
        private Decision decision;

        @JsonProperty("AssociatedAdvice")
        private List<Assignments> associatedAdvice;

        @JsonProperty("Obligations")
        private List<Assignments> obligations;

        public Response() {
        }

        public Response(final Decision decision, final List<Assignments> associatedAdvice, final List<Assignments> obligations) {
            this.decision = decision;
            this.associatedAdvice = associatedAdvice;
            this.obligations = obligations;
        }

        public Decision getDecision() {
            return decision;
        }

        public List<Assignments> getAssociatedAdvice() {
            return associatedAdvice;
        }

        public List<Assignments> getObligations() {
            return obligations;
        }

        public void setDecision(final Decision decision) {
            this.decision = decision;
        }

        public void setAssociatedAdvice(final List<Assignments> associatedAdvice) {
            this.associatedAdvice = associatedAdvice;
        }

        public void setObligations(final List<Assignments> obligations) {
            this.obligations = obligations;
        }
    }

    public static class Assignments {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("AttributeAssignment")
        private List<AttributeAssignment> attributeAssignment;

        public Assignments() {
        }

        public Assignments(final String id, final List<XacmlResponse.AttributeAssignment> attributeAssignment) {
            this.id = id;
            this.attributeAssignment = attributeAssignment;
        }

        public String getId() {
            return id;
        }

        public List<XacmlResponse.AttributeAssignment> getAttributeAssignment() {
            return attributeAssignment;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setAttributeAssignment(final List<XacmlResponse.AttributeAssignment> attributeAssignment) {
            this.attributeAssignment = attributeAssignment;
        }
    }

    public static class AttributeAssignment {

        @JsonProperty("AttributeId")
        private String attributeId;

        @JsonProperty("Value")
        private String value;

        public AttributeAssignment() {
        }

        public AttributeAssignment(final String attributeId, final String value) {
            this.attributeId = attributeId;
            this.value = value;
        }

        public String getAttributeId() {
            return attributeId;
        }

        public String getValue() {
            return value;
        }

        public void setAttributeId(final String attributeId) {
            this.attributeId = attributeId;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }
}
