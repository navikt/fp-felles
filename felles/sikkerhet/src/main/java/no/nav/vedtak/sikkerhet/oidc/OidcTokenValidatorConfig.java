package no.nav.vedtak.sikkerhet.oidc;

import java.util.Map;
import java.util.stream.Collectors;

public class OidcTokenValidatorConfig {

    private static volatile OidcTokenValidatorConfig instance; // NOSONAR
    private final Map<String, OidcTokenValidator> validators;

    private OidcTokenValidatorConfig() {
        this(init());
    }

    private OidcTokenValidatorConfig(Map<String, OidcTokenValidator> validators) {
        this.validators = validators;
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
    static void setValidators(Map<String, OidcTokenValidator> validators) {
        instance = new OidcTokenValidatorConfig(validators);
    }

    public OidcTokenValidator getValidator(String issuer) {
        return validators.get(issuer);
    }

    private static Map<String, OidcTokenValidator> init() {
        return OidcProviderConfig.instance().getOidcProviders().stream()
                .collect(Collectors.toMap(
                    config -> config.getIssuer().toExternalForm(),
                    OidcTokenValidator::new));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [validators=" + validators + "]";
    }
}
