package no.nav.vedtak.felles.integrasjon.pdl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PDLExceptionExtension {
    private final String code;
    private final PDLExceptionDetails details;

    @JsonCreator
    public PDLExceptionExtension(@JsonProperty("code") String code, @JsonProperty("details") PDLExceptionDetails details) {
        this.code = code;
        this.details = details;
    }

    public PDLExceptionDetails getDetails() {
        return details;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [code=" + code + ", details=" + details + "]";
    }

}
