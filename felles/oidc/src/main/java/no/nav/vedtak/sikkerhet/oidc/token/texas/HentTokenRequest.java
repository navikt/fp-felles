package no.nav.vedtak.sikkerhet.oidc.token.texas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record HentTokenRequest(IdProvider identity_provider, String target, String resource, List<AuthorizationDetails> authorization_details,
                               Boolean skip_cache) {

    @Override
    public String toString() {
        return "HentTokenRequest{" +
            "identity_provider=" + identity_provider +
            ", target='" + target + '\'' +
            (skip_cache != null ? ", skip_cache='" + skip_cache + '\'' : "") +
            (resource != null ? ", resource='" + resource + '\'' : "") +
            (authorization_details != null && !authorization_details.isEmpty() ? ", authorization_details=<redacted>" : "") +
            '}';
    }
}
