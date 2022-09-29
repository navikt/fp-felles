package no.nav.vedtak.isso.ressurs;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.isso.config.ServerInfo;

public final class AzureConfigProperties {

    private static final Environment ENV = Environment.current();

    // En mellomrom-separert liste over scopes man ønsker inkludert i token - i starten brukes openid
    // NB: dersom denne settes - så begynn med openid offline_access api://.... - men kun for den som gjør redirect
    // Fx openid offline_access api://<cluster>.<namespace>.fplos/.default
    private static final String AZURE_SCOPES_PROPERTY_NAME = "fp.trial.azure.scopes";

    // Sett = true for å aktivere
    private static final String AZURE_TRIAL_ENABLED = "fp.trial.azure.enabled";
    private static final String AZURE_TRIAL_CALLBACK = "fp.trial.azure.callback";
    private static final String AZURE_TRIAL_DOMAIN = "fp.trial.azure.domain";

    private static final String OPENID_SCOPE = "openid offline_access";


    private static final String AZURE_SCOPES = Optional.ofNullable(ENV.getProperty(AZURE_SCOPES_PROPERTY_NAME)).orElse(OPENID_SCOPE);
    private static final boolean AZURE_ENABLED  = Optional.ofNullable(ENV.getProperty(AZURE_TRIAL_ENABLED)).filter("true"::equals).isPresent();

    private AzureConfigProperties() {

    }


    public static boolean isAzureEnabled() {
        return AZURE_ENABLED;
    }

    public static String getAzureScopes() {
        return AZURE_SCOPES;
    }

    public static String getAzureCallback() {
        return  Optional.ofNullable(ENV.getProperty(AZURE_TRIAL_CALLBACK)).orElseGet(() -> ServerInfo.instance().getCallbackUrl());
    }

    public static String getAzureDomain() {
        return ENV.getProperty(AZURE_TRIAL_DOMAIN);
    }
}
