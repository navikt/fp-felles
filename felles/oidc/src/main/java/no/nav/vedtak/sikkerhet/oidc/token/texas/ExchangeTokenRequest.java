package no.nav.vedtak.sikkerhet.oidc.token.texas;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ExchangeTokenRequest(IdProvider identity_provider, String user_token, String target, Boolean skip_cache) {

    @Override
    public String toString() {
        return "ExchangeTokenRequest{" +
            "identity_provider=" + identity_provider +
            ", user_token=<redacted>" +
            ", target='" + target +
            (skip_cache != null ? ", skip_cache='" + skip_cache + '\'' : "") +
            '}';
    }

    public ExchangeTokenRequest(IdProvider identity_provider, String user_token, String target) {
        this(identity_provider, user_token, target, null);
    }
}
