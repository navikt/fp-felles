package no.nav.vedtak.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * Bruk heller Caffeine
 * Hvorfor ? Vi driver ikke avansert caching og en plain sak gjør jobben
 *
 */
@Deprecated(since = "4.0.x", forRemoval = true)
public class LRUCache<K extends Comparable<?>, V> {
    private final long timeoutMillis;
    private final Map<K, Item<V>> cacheMap;

    public LRUCache(int cacheSize, long timeoutMillis) {
        cacheMap = Collections.synchronizedMap(new LRUCacheMap<>(cacheSize));
        this.timeoutMillis = timeoutMillis;
    }

    public V get(K key) {
        Item<V> item = cacheMap.get(key);
        if (item == null) {
            return null;
        }

        if (System.currentTimeMillis() > item.getExpires()) {
            cacheMap.remove(key);
            return null;
        }
        return item.getValue();
    }

    public void put(K key, V value) {
        cacheMap.put(key, new Item<>(value, System.currentTimeMillis() + timeoutMillis));
    }

    public int size() {
        return cacheMap.size();
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    private static class Item<V> {
        private long expires;
        private V value;

        Item(V value, long expires) {
            this.value = value;
            this.expires = expires;
        }

        long getExpires() {
            return expires;
        }

        V getValue() {
            return value;
        }
    }

    private static class LRUCacheMap<K, V> extends LinkedHashMap<K, V> {
        private int cacheSize;

        LRUCacheMap(int cacheSize) {
            super(16, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return super.size() > cacheSize;
        }
    }
}
