package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GroupsProviderTest {

    @Test
    void testGroupsForLocal() {
        var provider = GroupsProvider.instance();
        assertThat(provider.getGroupValue(Groups.BESLUTTER)).isEqualTo("beslutter");
        assertThat(provider.getGroupFrom("drift")).isEqualTo(Groups.DRIFT);
        var grupper = List.of("saksbehandler", "beslutter");
        assertThat(provider.getGroupsFrom(grupper)).containsExactly(Groups.SAKSBEHANDLER, Groups.BESLUTTER);
    }


}
