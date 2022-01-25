package no.nav.vedtak.sikkerhet.oidc;

import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;

public class OidcTokenValidatorConfig {

    private static volatile OidcTokenValidatorConfig instance; // NOSONAR
    private final Map<String, OidcTokenValidator> validators;

    private OidcTokenValidatorConfig() {
        this.validators = new LinkedHashMap<>();
    }

    public static synchronized OidcTokenValidatorConfig instance() {
        var inst= instance;
        if (inst == null) {
            inst = new OidcTokenValidatorConfig();
            instance = inst;
        }
        return inst;
    }

    // For test
    static void addValidator(String issuer, OidcTokenValidator validator) {
        instance = new OidcTokenValidatorConfig();
        instance.validators.put(issuer, validator);
    }

    public OidcTokenValidator getValidator(String issuer) {
        var validator = validators.get(issuer);
        if (validator == null) {
            var cfg = ConfigProvider.getOpenIDConfiguration(issuer);
            validator = cfg.map(OidcTokenValidator::new).orElse(null);
            if (validator != null) {
                validators.put(issuer, validator);
            }
        }
        return validator;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [validators=" + validators + "]";
    }
}
