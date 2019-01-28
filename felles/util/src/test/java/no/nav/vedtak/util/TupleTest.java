package no.nav.vedtak.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TupleTest {

    @Test
    public void test_ctor_og_getters() {

        Tuple<String, Integer> tuple = new Tuple<>("ab", 3);

        assertThat(tuple.getElement1()).isEqualTo("ab");
        assertThat(tuple.getElement2()).isEqualTo(3);
    }

    @Test
    public void test_equals_felter() {

        Tuple<String, Integer> tuple1 = new Tuple<>("ab", 3);
        Tuple<String, Integer> tuple2 = new Tuple<>("ab", 3);
        Tuple<String, Integer> tuple3 = new Tuple<>("ab", 100);
        Tuple<String, Integer> tuple4 = new Tuple<>("c", 3);

        assertThat(tuple1.equals(tuple2)).isTrue();
        assertThat(tuple1.hashCode()).isEqualTo(tuple2.hashCode());
        assertThat(tuple1.equals(tuple3)).isFalse();
        assertThat(tuple1.equals(tuple4)).isFalse();
    }

    @Test
    public void test_equals_reflexive() {

        Tuple<String, Integer> tuple = new Tuple<>("ab", 3);

        assertThat(tuple.equals(tuple)).isTrue();
    }

    @Test
    public void test_equals_symmetric() {

        Tuple<String, Integer> tuple1 = new Tuple<>("ab", 3);
        Tuple<String, Integer> tuple2 = new Tuple<>("ab", 3);

        assertThat(tuple1.equals(tuple2)).isTrue();
        assertThat(tuple2.equals(tuple1)).isTrue();

        Tuple<String, Integer> tuple3 = new Tuple<>("c", 3);

        assertThat(tuple1.equals(tuple3)).isFalse();
        assertThat(tuple3.equals(tuple1)).isFalse();
    }

    @Test
    public void test_equals_transitive() {

        Tuple<String, Integer> tuple1 = new Tuple<>("ab", 3);
        Tuple<String, Integer> tuple2 = new Tuple<>("ab", 3);
        Tuple<String, Integer> tuple3 = new Tuple<>("ab", 3);

        assertThat(tuple1.equals(tuple2)).isTrue();
        assertThat(tuple1.hashCode()).isEqualTo(tuple2.hashCode());
        assertThat(tuple2.equals(tuple3)).isTrue();
        assertThat(tuple2.hashCode()).isEqualTo(tuple3.hashCode());
        assertThat(tuple1.equals(tuple3)).isTrue();
        assertThat(tuple1.hashCode()).isEqualTo(tuple3.hashCode());
    }

    @Test
    public void test_equals_null() {

        Tuple<String, Integer> tuple1 = new Tuple<>("ab", 3);

        assertThat(tuple1.equals(null)).isFalse();
    }
}