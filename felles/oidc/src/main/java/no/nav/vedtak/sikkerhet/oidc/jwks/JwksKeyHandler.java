package no.nav.vedtak.sikkerhet.oidc.jwks;

import java.security.Key;

public interface JwksKeyHandler {
    Key getValidationKey(JwtHeader header);
}
