package no.nav.vedtak.sikkerhet.loginmodule.saml;

import java.util.Collections;

import javax.security.auth.login.AppConfigurationEntry;

import no.nav.vedtak.sikkerhet.loginmodule.LoginConfiguration;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;

/** Pluggbar login config for SAML. Plugges inn i {@link LoginContextConfiguration} vha. ServiceLoader. */
public class SamlLoginConfiguration implements LoginConfiguration {

    @Override
    public String getName() {
        return "SAML";
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry() {
        return new AppConfigurationEntry[] {
                new AppConfigurationEntry(
                    "no.nav.vedtak.sikkerhet.loginmodule.saml.SamlLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                    Collections.emptyMap()),
                new AppConfigurationEntry(
                    "no.nav.vedtak.sikkerhet.loginmodule.ThreadLocalLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    Collections.emptyMap())
        };
    }

}
