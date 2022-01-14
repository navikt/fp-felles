package no.nav.vedtak.felles.integrasjon.rest.tokenhenter;

public record OidcTokenResponse(String access_token,
                                String token_type,
                                Integer expires_in) {
}
