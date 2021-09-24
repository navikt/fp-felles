package no.nav.vedtak.sikkerhet.oidc;

import java.util.Map;
import java.util.stream.Collectors;

public class OidcTokenValidatorProvider {

    private static volatile OidcTokenValidatorProvider instance; // NOSONAR
    private final Map<String, OidcTokenValidator> validators;

    private OidcTokenValidatorProvider() {
        this(init());
    }

    private OidcTokenValidatorProvider(Map<String, OidcTokenValidator> validators) {
        this.validators = validators;
    }

    public static OidcTokenValidatorProvider instance() {
        var inst= instance;
        if (inst == null) {
            inst = new OidcTokenValidatorProvider();
            instance = inst;
        }
        return inst;
    }

    // For test
    static void setValidators(Map<String, OidcTokenValidator> validators) {
        instance = new OidcTokenValidatorProvider(validators);
    }

    public OidcTokenValidator getValidator(String issuer) {
        return validators.get(issuer);
    }

    private static Map<String, OidcTokenValidator> init() {
        var configs = new OpenIDProviderConfigProvider().getConfigs();
        return configs.stream().collect(Collectors.toMap(
                config -> config.getIssuer().toExternalForm(),
                OidcTokenValidator::new));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [validators=" + validators + "]";
    }
}
