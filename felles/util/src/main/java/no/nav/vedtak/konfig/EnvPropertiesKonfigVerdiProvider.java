package no.nav.vedtak.konfig;

import static no.nav.vedtak.konfig.StandardPropertySource.ENV_PROPERTIES;

import java.util.Optional;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.konfig.KonfigVerdi.Converter;

@ApplicationScoped

public class EnvPropertiesKonfigVerdiProvider extends PropertiesKonfigVerdiProvider {

    public static final int PRIORITET = SystemPropertiesKonfigVerdiProvider.PRIORITET + 1;

    public EnvPropertiesKonfigVerdiProvider() {
        super(getEnv(), ENV_PROPERTIES);
    }

    @Override
    public <V> V getVerdi(String key, Converter<V> converter) {
        return Optional.ofNullable(super.getVerdi(key, converter))
                .orElse(super.getVerdi(upper(key), converter));
    }

    @Override
    public boolean harVerdi(String key) {
        return super.harVerdi(key) || super.harVerdi(upper(key));
    }

    private static String upper(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static Properties getEnv() {
        var p = new Properties();
        p.putAll(System.getenv());
        return p;
    }

    @Override
    public int getPrioritet() {
        return PRIORITET;
    }
}
