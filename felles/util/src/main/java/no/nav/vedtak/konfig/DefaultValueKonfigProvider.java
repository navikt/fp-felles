package no.nav.vedtak.konfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.vedtak.konfig.KonfigVerdi.Converter;

public class DefaultValueKonfigProvider implements KonfigVerdiProvider {

    @Override
    public <V> V getVerdi(String verdi, Converter<V> converter) {
        return converter.tilVerdi(verdi);
    }

    @Override
    public <V> List<V> getVerdier(String verdier, Converter<V> converter) {
        return Arrays.stream(verdier.split(",\\s*"))
                .map(converter::tilVerdi)
                .collect(Collectors.toList());

    }

    @Override
    public <V> Map<String, V> getVerdierAsMap(String verdier, Converter<V> converter) {
        return Arrays.stream(verdier.split(",\\s*"))
                .map(s -> s.split(":\\s*"))
                .collect(
                        Collectors.toMap(
                                e -> e[0],
                                e -> converter.tilVerdi(e[1]) // NOSONAR
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