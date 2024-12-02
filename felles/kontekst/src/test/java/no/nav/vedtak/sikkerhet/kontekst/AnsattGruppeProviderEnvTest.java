package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tester at environment tar presedens over oids fra bundlete ressursfiler
 */
class AnsattGruppeProviderEnvTest {

    @BeforeAll
    public static void setUp() {
        System.setProperty("gruppe.oid.beslutter", "6e31f9db-7e46-409d-809c-143d3863e4f6");
        System.setProperty("gruppe.oid.overstyrer", "542269ee-090b-4017-bbcc-6791580290ac");
    }

    @AfterAll
    public static void teardown() {
        System.clearProperty("gruppe.oid.beslutter");
        System.clearProperty("gruppe.oid.overstyrer");
    }


    @Test
    void testStringGroupsForLocal() {
        AnsattGruppeProvider.refresh();
        var provider = AnsattGruppeProvider.instance();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER)).isNotNull();
        assertThat(provider.getAnsattGruppeOid(AnsattGruppe.BESLUTTER).toString()).isEqualTo("6e31f9db-7e46-409d-809c-143d3863e4f6");
        assertThat(provider.getAnsattGruppeFra("89c71f0c-ca57-4e6f-8545-990f9e24c762")).isEqualTo(AnsattGruppe.DRIFTER);
        var grupper = List.of("eb211c0d-9ca6-467f-8863-9def2cc06fd3", "542269ee-090b-4017-bbcc-6791580290ac");
        assertThat(provider.getAnsattGrupperFraStrings(grupper)).containsAll(Set.of(AnsattGruppe.SAKSBEHANDLER, AnsattGruppe.OVERSTYRER));

        assertThat(provider.getAnsattGruppeFra((String) null)).isNull();
        assertThat(provider.getAnsattGruppeFra("forsvarer")).isNull();
        assertThat(provider.getAnsattGrupperFraStrings(List.of("forsvarer"))).isEmpty();
    }



}
