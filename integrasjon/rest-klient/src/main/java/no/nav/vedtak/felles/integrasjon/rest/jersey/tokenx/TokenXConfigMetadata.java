package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import com.fasterxml.jackson.annotation.JsonProperty;

record TokenXConfigMetadata(String issuer,
        @JsonProperty("token_endpoint") String tokenEndpoint,
        @JsonProperty("jwks_uri") String jwksUri) {
}
