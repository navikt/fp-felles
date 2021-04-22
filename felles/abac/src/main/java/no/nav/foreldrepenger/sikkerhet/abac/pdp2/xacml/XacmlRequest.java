package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

public class XacmlRequest {

    @JsonProperty("Request")
    private final Request request;

    public XacmlRequest(final XacmlRequest.Request request) {
        this.request = request;
    }

    public XacmlRequest.Request getRequest() {
        return request;
    }

    @Override
    public String toString() {
        return "XacmlRequest{" +
            "request=" + request +
            '}';
    }

    public static class Request {

        @JsonProperty("Action")
        private final AttributeSet action;

        @JsonProperty("Environment")
        private final AttributeSet environment;

        @JsonProperty("Resource")
        private final List<AttributeSet> resource;

        @JsonProperty("AccessSubject")
        private final AttributeSet accessSubject;

        public Request(final AttributeSet action, final AttributeSet environment, final List<AttributeSet> resource, final AttributeSet accessSubject) {
            this.action = action;
            this.environment = environment;
            this.resource = resource;
            this.accessSubject = accessSubject;
        }

        public AttributeSet getAction() {
            return action;
        }

        public AttributeSet getEnvironment() {
            return environment;
        }

        public List<AttributeSet> getResource() {
            return resource;
        }

        public AttributeSet getAccessSubject() {
            return accessSubject;
        }

        @Override
        public String toString() {
            return "Request{" +
                "action=" + action +
                ", environment=" + environment +
                ", resource=" + resource.stream().map(AttributeSet::toString).collect(Collectors.joining(", ", "{", "}")) +
                ", accessSubject=" + accessSubject +
                '}';
        }
    }

    public static class AttributeSet {
        @JsonProperty("Attribute")
        private final List<Pair> attribute;

        public AttributeSet(final List<Pair> attributt) {
            this.attribute = attributt;
        }

        public List<Pair> getAttribute() {
            return attribute;
        }

        @Override
        public String toString() {
            return "AttributeSet{" +
                "attribute=" + attribute.stream().map(Pair::toString).collect(Collectors.joining(", ", "{", "}")) +
                '}';
        }
    }

    public static class Pair {

        @JsonProperty("AttributeId")
        protected String attributeId;

        @JsonProperty("Value")
        protected Object value;

        public Pair(final String attributeId, final Object value) {
            this.attributeId = attributeId;
            this.value = value;
        }

        public String getAttributeId() {
            return attributeId;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Pair{" +
                "attributeId='" + attributeId + '\'' +
                ", value=" + value +
                '}';
        }
    }
}
