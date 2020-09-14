package no.nav.vedtak.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    public void skal_gjennkjenne_blankStreng() {

        assertThat(StringUtils.isBlank(null)).isTrue();
        assertThat(StringUtils.isBlank("")).isTrue();
        assertThat(StringUtils.isBlank(" ")).isTrue();
        assertThat(StringUtils.isBlank("  ")).isTrue();
        assertThat(StringUtils.isBlank("\t")).isTrue();

        assertThat(StringUtils.isBlank("a")).isFalse();
        assertThat(StringUtils.isBlank(" a")).isFalse();
        assertThat(StringUtils.isBlank(" a ")).isFalse();
        assertThat(StringUtils.isBlank("a ")).isFalse();
    }

    @Test
    public void skal_gjennkjenne_nullEllerTomStreng() {

        assertThat(StringUtils.nullOrEmpty(null)).isTrue();
        assertThat(StringUtils.nullOrEmpty("")).isTrue();

        assertThat(StringUtils.nullOrEmpty(" ")).isFalse();
        assertThat(StringUtils.nullOrEmpty("a")).isFalse();
    }
}
