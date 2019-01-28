package no.nav.vedtak.sikkerhet.loginmodule;

import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@ApplicationScoped
public class LoginContextConfiguration extends Configuration {

    private final Map<String, AppConfigurationEntry[]> conf = new HashMap<>();

    public LoginContextConfiguration() {
        replaceConfiguration(LoginConfigNames.OIDC.name(), lagConfigOidc());
        replaceConfiguration(LoginConfigNames.SAML.name(), lagConfigSaml());
        replaceConfiguration(LoginConfigNames.TASK_OIDC.name(), lagConfigTaskOidc());
    }

    /**
     * Associates the specified configurationEntries with the specified configName in this configuration.
     * If the configuration previously contained a mapping for the configName, the old configurationEntries is replaced
     * by the specified configurationEntries.
     *
     * @param configName           configName with which the specified configurationEntries is to be associated
     * @param configurationEntries configurationEntries to be associated with the specified configName
     */
    protected void replaceConfiguration(String configName, AppConfigurationEntry[] configurationEntries) {
        conf.put(configName, configurationEntries);
    }

    private AppConfigurationEntry[] lagConfigOidc() {
        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.OIDCLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Collections.emptyMap())
        };
    }

    private AppConfigurationEntry[] lagConfigSaml() {
        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.SamlLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Collections.emptyMap()),
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.ThreadLocalLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Collections.emptyMap())
        };
    }

    private AppConfigurationEntry[] lagConfigTaskOidc() {
        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.OIDCLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Collections.emptyMap()),
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.ThreadLocalLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Collections.emptyMap())
        };
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        checkArgument(conf.containsKey(name), "Har ikke konfigurasjon for: " + name);
        return conf.get(name);
    }
}