package no.nav.vedtak.log.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

class MdcExtendedLogContextTest {

    private final MdcExtendedLogContext context = MdcExtendedLogContext.getContext("prosess");

    @AfterEach
    void clear() {
        MDC.clear();
    }

    @Test
    void skal_legge_til_ny_verdi() {

        context.add("behandling", 1L);
        assertThat(context.get("behandling")).isEqualTo("1");

        context.add("fagsak", 2L);
        assertThat(context.get("fagsak")).isEqualTo("2");

        context.add("steg", "sistesteg");
        assertThat(context.get("steg")).isEqualTo("sistesteg");
    }

    @Test
    void skal_fjerne_verdi() {
        context.add("behandling", 1L);
        context.add("fagsak", 2L);
        context.add("prosess", 3L);

        context.remove("behandling");
        assertThat(context.get("behandling")).isNull();

        context.remove("fagsak");
        assertThat(context.get("fagsak")).isNull();

        context.remove("steg");
        assertThat(context.get("steg")).isNull();

    }

}
