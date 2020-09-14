package no.nav.vedtak.log.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.log.mdc.MdcExtendedLogContext;

public class MdcExtendedLogContextTest {

    private MdcExtendedLogContext context = MdcExtendedLogContext.getContext("prosess");

    @AfterEach
    public void clear() {
        context.clear();
    }

    @Test
    public void skal_legge_til_ny_verdi() throws Exception {

        context.add("behandling", 1L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1]");

        context.add("fagsak", 2L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2]");

        context.add("prosess", 3L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");
    }

    @Test
    public void skal_hente_key_part() throws Exception {
        context.add("behandling", 1L);
        context.add("fagsak", 2L);
        context.add("prosess", 3L);
        assertThat(context.getFullText()).isEqualTo("prosess[behandling=1;fagsak=2;prosess=3]");

        assertThat(context.getValue("behandling")).isEqualTo("1");
        assertThat(context.getValue("fagsak")).isEqualTo("2");
        assertThat(context.getValue("prosess")).isEqualTo("3");
    }

    @Test
    public void skal_fjerne_verdi() throws Exception {
        // Arrange
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
    public void skal_fjerne_verdi_i_midten() throws Exception {
        // Arrange
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
    public void skal_sjekke_ugyldig_key_left_bracket() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fdss[", 1L));
    }

    @Test
    public void skal_sjekke_ugyldig_key_right_bracket() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fd]ss", 1L));
    }

    @Test
    public void skal_sjekke_ugyldig_key_semicolon() {
        assertThrows(IllegalArgumentException.class, () -> context.add("fdss;", 1L));
    }
}
