package no.nav.vedtak.sikkerhet.abac.policy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.Token;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursInterceptorTest;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipOverstyring;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

@ExtendWith(MockitoExtension.class)
class InternBrukerFagsakTest {


    @Test
    void tilgang_fagsak_read_veileder() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.READ);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("veileder")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void avslå_fagsak_create_veileder() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.CREATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medAnsvarligSaksbehandler("veileder")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslå_fagsak_update_veileder() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("veileder")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tilgang_fagsak_read() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_fagsak_read1() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_fagsak_read2() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ);
        var ressurs = AppRessursData.builder()
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_fagsak_create() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.CREATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void avslag_fagsak_update_uten_status() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tillat_fagsak_update_vanlig() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tillat_fagsak_update_beslutter() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.FATTE_VEDTAK)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.BESLUTTER)).isTrue();
    }

    @Test
    void avslå_fagsak_update_beslutter_lik_saksbehandler() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.FATTE_VEDTAK)
            .medAnsvarligSaksbehandler("IBRUKER")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tillat_fagsak_update_overstyr() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medOverstyring(PipOverstyring.OVERSTYRING)
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OVERSTYRER)).isTrue();
    }

    @Test
    void avslå_fagsak_update_overstyr_fved() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.FATTE_VEDTAK)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medOverstyring(PipOverstyring.OVERSTYRING)
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslå_fagsak_update_overstyr_opprett() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.OPPRETTET)
            .medAnsvarligSaksbehandler("saksbehandler")
            .medOverstyring(PipOverstyring.OVERSTYRING)
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_fagsak_delete() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.DELETE);
        var ressurs = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES)
            .medAnsvarligSaksbehandler("saksbehandler")
            .build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    private BeskyttetRessursAttributter lagAttributter(AnsattGruppe ansattGruppe, ActionType actionType) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId("IBRUKER")
            .medBrukerOid(UUID.randomUUID())
            .medIdentType(IdentType.InternBruker)
            .medAnsattGrupper(Set.of(ansattGruppe))
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(ResourceType.FAGSAK)
            .medActionType(actionType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
