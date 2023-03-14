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
}
