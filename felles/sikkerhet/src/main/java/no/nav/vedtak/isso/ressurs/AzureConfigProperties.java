package no.nav.vedtak.isso.ressurs;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;

public final class AzureConfigProperties {

    private static final Environment ENV = Environment.current();

    // En *-separert liste over scopes man ønsker inkludert i token - i starten brukes openid
    // Fx api://<cluster>:<namespace>:fplos/default*api://<cluster>:<namespace>:fpsak/default
    private static final String AZURE_SCOPES_PROPERTY_NAME = "fp.trial.azure.scopes";

    // Sett = true for å aktivere
    private static final String AZURE_TRIAL_ENABLED = "fp.trial.azure.enabled";

    private static final String OPENID_SCOPE = "openid";


    private static final String AZURE_SCOPES = Optional.ofNullable(ENV.getProperty(AZURE_SCOPES_PROPERTY_NAME))
        .map(s -> s.replace("\\*", " ")).orElse(OPENID_SCOPE);
    private static final boolean AZURE_ENABLED  = Optional.ofNullable(ENV.getProperty(AZURE_TRIAL_ENABLED)).filter("true"::equals).isPresent();

    private AzureConfigProperties() {

    }


    public static boolean isAzureEnabled() {
        return AZURE_ENABLED;
    }

    public static String getAzureScopes() {
        return AZURE_SCOPES;
    }
}
