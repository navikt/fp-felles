package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GroupsProviderTest {

    @Test
    void testGroupsForLocal() {
        var provider = GroupsProvider.instance();
        assertThat(provider.getGroupValue(Groups.BESLUTTER)).isEqualTo("beslutter");
        assertThat(provider.getGroupFrom("drift")).isEqualTo(Groups.DRIFT);
        var grupper = List.of("saksbehandler", "overstyrer");
        assertThat(provider.getGroupsFrom(grupper)).containsAll(Set.of(Groups.SAKSBEHANDLER, Groups.OVERSTYRER));

        assertThat(provider.getGroupFrom(null)).isNull();
        assertThat(provider.getGroupFrom("forsvarer")).isNull();
        assertThat(provider.getGroupsFrom(List.of("forsvarer"))).isEmpty();
    }


}
