package no.nav.vedtak.konfig;

import java.util.List;
import java.util.Map;

/**
 * Provider som kan slå opp verdi for en angitt key
 */
@Deprecated(since = "3.1", forRemoval = true)
/* Bruk klasser fra no.nav.foreldrepenger:konfig:1.1 istedenfor. */
public interface KonfigVerdiProvider {

    /* Get verdi for angitt key. */
    <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter);

    <V> List<V> getVerdier(String key, KonfigVerdi.Converter<V> converter);

    <V> Map<String, V> getVerdierAsMap(String key, KonfigVerdi.Converter<V> converter);

    boolean harVerdi(String key);

    StandardPropertySource getSource();

    /* Prioritet rekkefølge. 1 er høyest prioritet. */
    int getPrioritet();

    PropertySourceMetaData getAllProperties();

}
