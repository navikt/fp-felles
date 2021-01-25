package no.nav.vedtak.felles.integrasjon.pdl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PDLExceptionDetails {
    private final String type;
    private final String cause;
    private final String policy;

    @JsonCreator
    public PDLExceptionDetails(@JsonProperty("type") String type, @JsonProperty("cause") String cause, @JsonProperty("policy") String policy) {
        this.type = type;
        this.cause = cause;
        this.policy = policy;
    }

    public String getType() {
        return type;
    }

    public String getCause() {
        return cause;
    }

    public String getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [type=" + type + ", cause=" + cause + ", policy=" + policy + "]";
    }
}
