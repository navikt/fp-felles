package no.nav.vedtak.log.util;

import no.nav.vedtak.log.mdc.MdcExtendedLogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MdcExtendedLogContextTest {

    private MdcExtendedLogContext context = MdcExtendedLogContext.getContext("prosess");

    @AfterEach
    void clear() {
        MDC.clear();
    }

    @Test
    void skal_legge_til_ny_verdi() throws Exception {

        context.add("behandling", 1L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1]");

        context.add("fagsak", 2L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2]");

        context.add("prosess", 3L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");
    }

    @Test
    void skal_hente_key_part() throws Exception {
        context.add("behandling", 1L);
        context.add("fagsak", 2L);
        context.add("prosess", 3L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");
    }

    @Test
    void skal_fjerne_verdi() throws Exception {
        context.add("behandling", 1L);
        context.add("fagsak", 2L);
        context.add("prosess", 3L);

        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");

        context.remove("behandling");
        assertThat(context.getFullText()).isEqualTo("prosess[fagsak=2;prosess=3]");

        context.remove("prosess");
        assertThat(context.getFullText()).isEqualTo("prosess[fagsak=2]");

        context.remove("fagsak");
        assertThat(context.getFullText()).isNull();

    }

    @Test
    void skal_fjerne_verdi_i_midten() throws Exception {
        context.add("behandling", 1L);
        context.add("fagsak", 2L);
        context.add("prosess", 3L);

        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");

        context.remove("fagsak");
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;prosess=3]");

        context.remove("behandling");
        assertThat(context.getFullText()).isEqualTo("prosess[prosess=3]");

        context.remove("prosess");
        assertThat(context.getFullText()).isNull();

    }

    @Test
    void skal_sjekke_ugyldig_key_left_bracket() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fdss[", 1L));
    }

    @Test
    void skal_sjekke_ugyldig_key_right_bracket() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fd]ss", 1L));
    }

    @Test
    void skal_sjekke_ugyldig_key_semicolon() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fdss;", 1L));
    }
}
