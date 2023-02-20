package no.nav.vedtak.sikkerhet.jaspic;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;

public class OidcTokenValidatorProviderForTest {

    // Exposing for test
    public static void setValidators(OpenIDProvider provider, OidcTokenValidator validator) {
        OidcTokenValidatorConfig.addValidator(provider, validator);
    }
}
