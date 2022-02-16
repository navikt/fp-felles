package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class OidcTokenValidatorProviderForTest {

    // Exposing for test
    public static void setValidators(OpenIDProvider provider, OidcTokenValidator validator) {
        OidcTokenValidatorConfig.addValidator(provider, validator);
    }
}
