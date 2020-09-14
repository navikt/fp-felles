package no.nav.vedtak.sikkerhet.loginmodule.oidc;

import java.util.Collections;

import javax.security.auth.login.AppConfigurationEntry;

import no.nav.vedtak.sikkerhet.loginmodule.LoginConfiguration;

public class OIDCLoginConfiguration implements LoginConfiguration {

    private static final AppConfigurationEntry[] OIDC_CONFIGURATION = new AppConfigurationEntry[] {
            new AppConfigurationEntry(
                "no.nav.vedtak.sikkerhet.loginmodule.oidc.OIDCLoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                Collections.emptyMap())
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
