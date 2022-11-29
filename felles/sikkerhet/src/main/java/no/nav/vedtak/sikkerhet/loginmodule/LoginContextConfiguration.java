package no.nav.vedtak.sikkerhet.loginmodule;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class LoginContextConfiguration extends Configuration {

    private static final Map<String, AppConfigurationEntry[]> CONFIGS = ServiceLoader.load(LoginConfiguration.class)
            .stream()
            .map(Provider::get)
            .collect(Collectors.toMap(LoginConfiguration::getName, LoginConfiguration::getAppConfigurationEntry));

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (!CONFIGS.containsKey(name)) {
            throw new IllegalArgumentException("Har ikke konfigurasjon for: " + name);
        }
        return CONFIGS.get(name);
    }
}
