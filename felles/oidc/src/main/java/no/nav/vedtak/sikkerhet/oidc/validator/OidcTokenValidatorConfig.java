package no.nav.vedtak.sikkerhet.oidc.validator;

import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class OidcTokenValidatorConfig {

    private static OidcTokenValidatorConfig instance;
    private final Map<OpenIDProvider, OidcTokenValidator> validators;

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
    public static void addValidator(OpenIDProvider provider, OidcTokenValidator validator) {
        instance = new OidcTokenValidatorConfig();
        instance.validators.put(provider, validator);
    }

    public OidcTokenValidator getValidator(OpenIDProvider provider) {
        var validator = validators.get(provider);
        if (validator == null) {
            var cfg = ConfigProvider.getOpenIDConfiguration(provider);
            validator = cfg.map(OidcTokenValidator::new).orElse(null);
            if (validator != null) {
                validators.put(provider, validator);
            }
        }
        return validator;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [validators=" + validators + "]";
    }
}
