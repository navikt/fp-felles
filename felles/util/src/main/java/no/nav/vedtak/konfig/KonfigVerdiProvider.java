package no.nav.vedtak.konfig;

import java.util.List;
import java.util.Map;

/**
 * Provider som kan slå opp verdi for en angitt key
 */
public interface KonfigVerdiProvider {

    /* Get verdi for angitt key. */
    <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter);
    
    <V> List<V> getVerdier(String key, KonfigVerdi.Converter<V> converter);
    
    <V> Map<String, V> getVerdierAsMap(String key, KonfigVerdi.Converter<V> converter);
    
    boolean harVerdi(String key);
    
    /* Prioritet rekkefølge.  1 er høyest prioritet. */
    int getPrioritet();

}
