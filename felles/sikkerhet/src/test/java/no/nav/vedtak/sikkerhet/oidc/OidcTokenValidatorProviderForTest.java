package no.nav.vedtak.sikkerhet.oidc;

import java.util.Map;

public class OidcTokenValidatorProviderForTest {

    // Exposing for test
    public static void setValidators(Map<String, OidcTokenValidator> validators) {
        OidcTokenValidatorConfig.setValidators(validators);
    }
}
