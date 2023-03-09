package no.nav.vedtak.sikkerhet.loginmodule.oidc;

import no.nav.vedtak.sikkerhet.loginmodule.LoginConfiguration;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.Map;

public class OIDCLoginConfiguration implements LoginConfiguration {

    private static final AppConfigurationEntry[] OIDC_CONFIGURATION = new AppConfigurationEntry[]{
        new AppConfigurationEntry(
            "no.nav.vedtak.sikkerhet.loginmodule.oidc.OIDCLoginModule",
            AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
            Map.of())
    };

    @Override
    public String getName() {
        return "OIDC";
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry() {
        return OIDC_CONFIGURATION;
    }

}
