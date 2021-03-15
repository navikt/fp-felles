package no.nav.foreldrepenger.konfig;

import java.util.Properties;

public class PropertySourceMetaData {
    private final StandardPropertySource source;
    private final Properties verdier;

    public PropertySourceMetaData(StandardPropertySource source, Properties verdier) {
        this.source = source;
        this.verdier = verdier;

    }

    public Properties getVerdier() {
        return verdier;
    }

    public StandardPropertySource getSource() {
        return source;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[source=" + source + ", verdier=" + verdier + "]";
    }
}
