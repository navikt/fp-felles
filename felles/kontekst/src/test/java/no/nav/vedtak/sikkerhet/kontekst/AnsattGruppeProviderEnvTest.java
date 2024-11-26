package no.nav.vedtak.sikkerhet.kontekst;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tester at environment tar presedens over oids fra bundlete ressursfiler
 */
class AnsattGruppeProviderEnvTest {

    public void setupAll() {
        System.setProperty(AnsattGruppeProvider.getPropertyNavn(AnsattGruppe.BESLUTTER), "6e31f9db-7e46-409d-809c-143d3863e4f6");
        System.setProperty(AnsattGruppeProvider.getPropertyNavn(AnsattGruppe.OVERSTYRER), "542269ee-090b-4017-bbcc-6791580290ac");
    }

    @BeforeEach
    public void setUp() {
        setupAll();
    }

    @AfterEach
    public void teardown() {
        System.clearProperty(AnsattGruppeProvider.getPropertyNavn(AnsattGruppe.BESLUTTER));
        System.clearProperty(AnsattGruppeProvider.getPropertyNavn(AnsattGruppe.OVERSTYRER));
    }


    @Test
    void testStringGroupsForLocal() {
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
