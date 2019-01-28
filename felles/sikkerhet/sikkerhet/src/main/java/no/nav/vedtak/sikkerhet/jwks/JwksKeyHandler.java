package no.nav.vedtak.sikkerhet.jwks;

import java.security.Key;

public interface JwksKeyHandler {
    Key getValidationKey(JwtHeader header);
}
