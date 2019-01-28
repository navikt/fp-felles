package no.nav.vedtak.konfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class PropertiesKonfigVerdiProvider implements KonfigVerdiProvider {

    private final Properties props;

    protected PropertiesKonfigVerdiProvider(Properties props) {
        Objects.requireNonNull(props, "props"); //$NON-NLS-1$
        this.props = props;
    }

    @Override
    public <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter) {
        return converter.tilVerdi(PropertyUtil.getProperty(key));
    }

    @Override
    public boolean harVerdi(String key) {
        return props.containsKey(key);
    }

    @Override
    public <V> List<V> getVerdier(String key, KonfigVerdi.Converter<V> converter) {
        String verdiString = props.getProperty(key);

        List<String> asList = Arrays.asList(verdiString.split(",\\s*")); //$NON-NLS-1$
        return asList.stream().map(v -> converter.tilVerdi(v)).collect(Collectors.toList());
    }

    @Override
    public <V> Map<String, V> getVerdierAsMap(String key, KonfigVerdi.Converter<V> converter) {
        String str = props.getProperty(key);
        Map<String, V> map = Arrays.asList(str.split(",\\s*")) //$NON-NLS-1$
                .stream()
                .map(s -> s.split(":\\s*")) //$NON-NLS-1$
                .collect(
                        Collectors.toMap(
                                e -> e[0], // NOSONAR
                                e -> converter.tilVerdi(e[1]) // NOSONAR
                        ));
        return map;
    }

}
