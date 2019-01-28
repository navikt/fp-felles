package no.nav.vedtak.konfig;

import java.util.List;
import java.util.Map;

import no.nav.vedtak.konfig.KonfigVerdi.Converter;

class KonfigVerdiProviderOutput {

    @SuppressWarnings("rawtypes")
    static final ProviderOutput SIMPLE = new VerdiOutput();
    @SuppressWarnings("rawtypes")
    static final ProviderOutput<List> LIST = new ListOutput();
    @SuppressWarnings("rawtypes")
    static final ProviderOutput<Map> MAP = new MapOutput();

    interface ProviderOutput<T> {
        T getOutput(KonfigVerdiProvider provider, String key, KonfigVerdi.Converter<?> converter);
    }

    @SuppressWarnings("rawtypes")
    static class ListOutput implements ProviderOutput<List> {

        @SuppressWarnings("unchecked")
        @Override
        public List getOutput(KonfigVerdiProvider provider, String key, Converter converter) {
            return provider.getVerdier(key, converter);
        }
    }

    @SuppressWarnings("rawtypes")
    static final class MapOutput implements ProviderOutput<Map> {

        @SuppressWarnings("unchecked")
        @Override
        public Map getOutput(KonfigVerdiProvider provider, String key, Converter converter) {
            return provider.getVerdierAsMap(key, converter);
        }
    }

    @SuppressWarnings("rawtypes")
    static class VerdiOutput implements ProviderOutput {
        @SuppressWarnings("unchecked")
        @Override
        public Object getOutput(KonfigVerdiProvider provider, String key, Converter converter) {
            return provider.getVerdi(key, converter);
        }
    }

}
