package no.nav.foreldrepenger.konfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class PropertiesKonfigVerdiProvider implements KonfigVerdiProvider {

    private final PropertySourceMetaData metadata;

    protected PropertiesKonfigVerdiProvider(Properties props, StandardPropertySource source) {
        this(new PropertySourceMetaData(source, props));
    }

    protected PropertiesKonfigVerdiProvider(PropertySourceMetaData metaData) {
        this.metadata = metaData;
    }

    @Override
    public StandardPropertySource getSource() {
        return metadata.getSource();
    }

    @Override
    public PropertySourceMetaData getAllProperties() {
        return metadata;
    }

    @Override
    public <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter) {
        return converter.tilVerdi((String) metadata.getVerdier().get(key));
    }

    @Override
    public boolean harVerdi(String key) {
        return metadata.getVerdier().containsKey(key);
    }

    @Override
    public <V> List<V> getVerdier(String key, KonfigVerdi.Converter<V> converter) {
        String verdiString = (String) metadata.getVerdier().get(key);
        List<String> asList = Arrays.asList(verdiString.split(",\\s*"));
        return asList.stream().map(converter::tilVerdi).toList();
    }

    @Override
    public <V> Map<String, V> getVerdierAsMap(String key, KonfigVerdi.Converter<V> converter) {
        String str = (String) metadata.getVerdier().get(key);

        return Arrays.stream(str.split(",\\s*"))
            .map(s -> s.split(":\\s*"))
            .collect(
                Collectors.toMap(
                    e -> e[0],
                    e -> converter.tilVerdi(e[1])
                ));
    }
}
