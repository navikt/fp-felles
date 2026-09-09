package no.nav.vedtak.log.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import no.nav.vedtak.log.mdc.LoggFelter;
import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

class MdcExtendedLogContextTest {

    private final MdcExtendedLogContext context = MdcExtendedLogContext.getContext("prosess");

    @AfterEach
    void clear() {
        MDC.clear();
    }

    @Test
    void skal_legge_til_ny_verdi() {

        context.add(LoggFelter.BEHANDLING, 1L);
        assertThat(context.get(LoggFelter.BEHANDLING)).isEqualTo("1");

        context.add(LoggFelter.SAK, 2L);
        assertThat(context.get(LoggFelter.SAK)).isEqualTo("2");

        context.add(LoggFelter.STEG, "sistesteg");
        assertThat(context.get(LoggFelter.STEG)).isEqualTo("sistesteg");
    }

    @Test
    void skal_fjerne_verdi() {
        context.add(LoggFelter.BEHANDLING, 1L);
        context.add(LoggFelter.SAK, 2L);
        context.add(LoggFelter.STEG, 3L);

        context.remove(LoggFelter.BEHANDLING);
        assertThat(context.get(LoggFelter.BEHANDLING)).isNull();

        context.remove(LoggFelter.SAK);
        assertThat(context.get(LoggFelter.SAK)).isNull();

        context.remove(LoggFelter.STEG);
        assertThat(context.get(LoggFelter.STEG)).isNull();

    }

}
