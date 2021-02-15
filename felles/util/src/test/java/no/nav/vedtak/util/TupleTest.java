package no.nav.vedtak.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TupleTest {

    @Test
    void test_ctor_og_getters() {
        var tuple = Tuple.of("ab", 3);
        assertThat(tuple.getElement1()).isEqualTo("ab");
        assertThat(tuple.getElement2()).isEqualTo(3);
    }

    @Test
    void test_equals_felter() {
        var tuple1 = Tuple.of("ab", 3);
        var tuple2 = Tuple.of("ab", 3);
        var tuple3 = Tuple.of("ab", 100);
        var tuple4 = Tuple.of("c", 3);
        assertThat(tuple1).isEqualTo(tuple2)
                .hasSameHashCodeAs(tuple2)
                .isNotEqualTo(tuple3)
                .isNotEqualTo(tuple4);
    }

    @Test
    void test_equals_reflexive() {
        var tuple = Tuple.of("ab", 3);
        assertThat(tuple).isEqualTo(tuple);
    }

    @Test
    void test_equals_symmetric() {
        var tuple1 = Tuple.of("ab", 3);
        var tuple2 = Tuple.of("ab", 3);
        assertThat(tuple1).isEqualTo(tuple2);
    }

    @Test
    void test_equals_transitive() {

        var tuple1 = Tuple.of("ab", 3);
        var tuple2 = Tuple.of("ab", 3);
        var tuple3 = Tuple.of("ab", 3);

        assertThat(tuple1).isEqualTo(tuple2)
                .hasSameHashCodeAs(tuple2)
                .isEqualTo(tuple3)
                .hasSameHashCodeAs(tuple3);

        assertThat(tuple2).isEqualTo(tuple3)
                .hasSameHashCodeAs(tuple3);
    }

    @Test
    void test_equals_null() {
        var tuple1 = Tuple.of("ab", 3);
        assertThat(tuple1).isNotEqualTo(null);
    }
}