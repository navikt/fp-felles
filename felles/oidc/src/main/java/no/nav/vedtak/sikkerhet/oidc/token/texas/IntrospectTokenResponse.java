package no.nav.vedtak.sikkerhet.oidc.token.texas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntrospectTokenResponse(
    boolean active,
    String error,
    String token_type,
    String aud,
    String azp,
    String azp_name,
    List<String> groups,
    List<String> roles,
    String tid,
    Long exp,
    Long iat,
    Long nbf,
    String iss,
    String jti,
    String oid,
    String sub,
    String NAVident,
    String idtyp,
    String acr,
    String pid,
    // Maskinporten
    String client_id,
    String client_amr,
    String scope,
    OrgDetails consumer,
    List<AuthorizationDetails> authorization_details) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuthorizationDetails(
        String type,
        String system_id,
        List<String> systemuser_id,
        OrgDetails systemuser_org) {
    }

    // kommer på følgende format i json: "0192:orgno"
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrgDetails(
        @JsonProperty("ID") String id,
        String authority) {
    }
}
