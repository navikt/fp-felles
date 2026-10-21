package no.nav.vedtak.log.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoggerUtilsTest {

    @Test
    void removeLineBreaksSkalFjerneCR() {

        assertThat(LoggerUtils.removeLineBreaks("\r")).isEmpty();
        assertThat(LoggerUtils.removeLineBreaks("a\rb")).isEqualTo("ab");
        assertThat(LoggerUtils.removeLineBreaks("\ra\rb")).isEqualTo("ab");
    }

    @Test
    void removeLineBreaksSkalFjerneLF() {

        assertThat(LoggerUtils.removeLineBreaks("\n")).isEmpty();
        assertThat(LoggerUtils.removeLineBreaks("a\nb")).isEqualTo("ab");
        assertThat(LoggerUtils.removeLineBreaks("\na\n\nb")).isEqualTo("ab");
    }

    @Test
    void removeLineBreaksSkalTakleNull() {
        assertThat(LoggerUtils.removeLineBreaks(null)).isNull();
    }

    @Test
    void maskDigitSequencesSkalMaskereTallrekkerPåFireEllerFlereSiffer() {
        assertThat(LoggerUtils.maskDigitSequences("/p/12345678901/a/1234567890123/s/152001234")).isEqualTo("/p/***/a/***/s/***");
        assertThat(LoggerUtils.maskDigitSequences("/fnr/010190-12345/dato/010190")).isEqualTo("/fnr/***-***/dato/***");
        assertThat(LoggerUtils.maskDigitSequences("/v1/annenPart/v123")).isEqualTo("/v1/annenPart/v123");
        assertThat(LoggerUtils.maskDigitSequences(null)).isNull();
    }
}
