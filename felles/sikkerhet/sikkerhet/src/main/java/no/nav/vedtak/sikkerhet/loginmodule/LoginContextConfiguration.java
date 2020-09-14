package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class LoginContextConfiguration extends Configuration {

    private final Map<String, AppConfigurationEntry[]> conf = new HashMap<>();

    public LoginContextConfiguration() {
        ServiceLoader.load(LoginConfiguration.class).forEach(c -> conf.put(c.getName(), c.getAppConfigurationEntry()));
    }

    /**
     * Associates the specified configurationEntries with the specified configName in this configuration.
     * If the configuration previously contained a mapping for the configName, the old configurationEntries is replaced
     * by the specified configurationEntries.
     *
     * @param configName configName with which the specified configurationEntries is to be associated
     * @param configurationEntries configurationEntries to be associated with the specified configName
     */
    protected void replaceConfiguration(String configName, AppConfigurationEntry[] configurationEntries) {
        conf.put(configName, configurationEntries);
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (!conf.containsKey(name)) {
            throw new IllegalArgumentException("Har ikke konfigurasjon for: " + name);
        }
        return conf.get(name);
    }
}