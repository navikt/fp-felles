package no.nav.foreldrepenger.konfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.konfig.KonfigVerdi.Converter;

public class DefaultValueKonfigProvider implements KonfigVerdiProvider {

    @Override
    public PropertySourceMetaData getAllProperties() {
        return new PropertySourceMetaData(StandardPropertySource.DEFAULT, new Properties());
    }

    @Override
    public StandardPropertySource getSource() {
        return StandardPropertySource.DEFAULT;
    }

    @Override
    public <V> V getVerdi(String verdi, Converter<V> converter) {
        return converter.tilVerdi(verdi);
    }

    @Override
    public <V> List<V> getVerdier(String verdier, Converter<V> converter) {
        return Arrays.stream(verdier.split(",\\s*"))
            .map(converter::tilVerdi)
            .toList();

    }

    @Override
    public <V> Map<String, V> getVerdierAsMap(String verdier, Converter<V> converter) {
        return Arrays.stream(verdier.split(",\\s*"))
            .map(s -> s.split(":\\s*"))
            .collect(
                Collectors.toMap(
                    e -> e[0],
                    e -> converter.tilVerdi(e[1])
                ));
    }

    @Override
    public boolean harVerdi(String key) {
        return true;
    }

    @Override
    public int getPrioritet() {
        return 0;
    }

}
