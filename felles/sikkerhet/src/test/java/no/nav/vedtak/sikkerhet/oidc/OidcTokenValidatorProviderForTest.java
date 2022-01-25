package no.nav.vedtak.sikkerhet.oidc;

public class OidcTokenValidatorProviderForTest {

    // Exposing for test
    public static void setValidators(String issuer, OidcTokenValidator validator) {
        OidcTokenValidatorConfig.addValidator(issuer, validator);
    }
}
