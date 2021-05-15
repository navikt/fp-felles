package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import com.fasterxml.jackson.annotation.JsonProperty;

record TokenXResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("issued_token_type") String issuedTokenType,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") String expiresIn) {
}
