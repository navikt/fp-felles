package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GroupsProviderTest {

    @Test
    void testGroupsForLocal() {
        var provider = GroupsProvider.instance();
        assertThat(provider.getGroupValue(Groups.BESLUTTER)).isEqualTo("803b1fd5-27a0-46a2-b1b3-7152f44128b4");
        assertThat(provider.getGroupFrom("89c71f0c-ca57-4e6f-8545-990f9e24c762")).isEqualTo(Groups.DRIFT);
        var grupper = List.of("eb211c0d-9ca6-467f-8863-9def2cc06fd3", "503f0cae-5bcd-484b-949c-a7e92d712858");
        assertThat(provider.getGroupsFrom(grupper)).containsAll(Set.of(Groups.SAKSBEHANDLER, Groups.OVERSTYRER));

        assertThat(provider.getGroupFrom(null)).isNull();
        assertThat(provider.getGroupFrom("forsvarer")).isNull();
        assertThat(provider.getGroupsFrom(List.of("forsvarer"))).isEmpty();
    }


}
