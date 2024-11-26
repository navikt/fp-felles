package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AnsattGruppeProviderTest {

    @Test
    void testStringGroupsForLocal() {
        var provider = AnsattGruppeProvider.instance();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER)).isNotNull();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER).toString()).isEqualTo("803b1fd5-27a0-46a2-b1b3-7152f44128b4");
        assertThat(provider.getAnsattGruppeFra("89c71f0c-ca57-4e6f-8545-990f9e24c762")).isEqualTo(AnsattGruppe.DRIFTER);
        var grupper = List.of("eb211c0d-9ca6-467f-8863-9def2cc06fd3", "503f0cae-5bcd-484b-949c-a7e92d712858");
        assertThat(provider.getAnsattGrupperFraStrings(grupper)).containsAll(Set.of(AnsattGruppe.SAKSBEHANDLER, AnsattGruppe.OVERSTYRER));

        assertThat(provider.getAnsattGruppeFra((String) null)).isNull();
        assertThat(provider.getAnsattGruppeFra("forsvarer")).isNull();
        assertThat(provider.getAnsattGrupperFraStrings(List.of("forsvarer"))).isEmpty();
    }

    @Test
    void testUuidGroupsForLocal() {
        var provider = AnsattGruppeProvider.instance();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER)).isNotNull();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER)).isEqualTo(UUID.fromString("803b1fd5-27a0-46a2-b1b3-7152f44128b4"));
        assertThat(provider.getAnsattGruppeFra(UUID.fromString("89c71f0c-ca57-4e6f-8545-990f9e24c762"))).isEqualTo(AnsattGruppe.DRIFTER);
        var grupper = List.of(UUID.fromString("eb211c0d-9ca6-467f-8863-9def2cc06fd3"), UUID.fromString("503f0cae-5bcd-484b-949c-a7e92d712858"));
        assertThat(provider.getAnsattGrupperFra(grupper)).containsAll(Set.of(AnsattGruppe.SAKSBEHANDLER, AnsattGruppe.OVERSTYRER));

        assertThat(provider.getAnsattGruppeFra((UUID) null)).isNull();
        assertThat(provider.getAnsattGruppeFra(UUID.fromString("eb211c0d-9ca6-467f-8863-9def2cc06fd4"))).isNull();
        assertThat(provider.getAnsattGrupperFra(List.of(UUID.fromString("eb211c0d-9ca6-467f-8863-9def2cc06fd4")))).isEmpty();
    }


}
