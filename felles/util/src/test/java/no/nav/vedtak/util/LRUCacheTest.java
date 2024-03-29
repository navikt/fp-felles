package no.nav.vedtak.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LRUCacheTest {

    private static final int CACHE_STØRRELSE = 2;
    private static final Long GYLDIGHET_I_MS = TimeUnit.MICROSECONDS.convert(1, TimeUnit.HOURS);

    private LRUCache<Long, String> cache;

    public LRUCacheTest() {
        this.cache = new LRUCache<>(CACHE_STØRRELSE, GYLDIGHET_I_MS);
    }

    @Test
    void skal_returnere_null_når_cache_er_tom() {
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void skal_legge_til_nye_innslag() {
        cache.put(1L, "1");
        assertThat(cache.get(1L)).isEqualTo("1");
        assertThat(cache.get(2L)).isNull();
        cache.put(2L, "4");
        assertThat(cache.get(1L)).isEqualTo("1");
        assertThat(cache.get(2L)).isEqualTo("4");
    }

    @Test
    void skal_slette_eldste_innslag_når_kapasiteten_er_nådd() {
        cache.put(1L, "1");
        cache.put(2L, "4");
        cache.put(3L, "9");
        assertThat(cache.size()).isEqualTo(CACHE_STØRRELSE);
        assertThat(cache.get(1L)).isNull();
        assertThat(cache.get(2L)).isEqualTo("4");
        assertThat(cache.get(3L)).isEqualTo("9");
    }

    @Test
    void skal_fornye_innslag() {
        cache.put(1L, "1");
        cache.put(2L, "4");
        assertThat(cache.get(1L)).isEqualTo("1");
        cache.put(3L, "9");
        assertThat(cache.get(1L)).isEqualTo("1");
        assertThat(cache.get(2L)).isNull();
        assertThat(cache.get(3L)).isEqualTo("9");
    }

    @Test
    void cache_innslag_skal_expire() throws Exception {
        this.cache = new LRUCache<>(CACHE_STØRRELSE, 2L);
        cache.put(1L, "1");
        Thread.sleep(4L); // NOSONAR
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void skal_legge_til_og_slette_key() {
        cache.put(1L, "1");
        assertThat(cache.get(1L)).isNotNull();
        cache.remove(1L);
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void skal_slette_key_som_ikke_eksister() {
        assertThat(cache.get(1L)).isNull();
        cache.remove(1L);
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void skal_oppdatere_eksisterende_key() {
        cache.put(1L, "1");
        cache.put(1L, "2");
        assertThat(cache.get(1L)).isEqualTo("2");
    }
}
