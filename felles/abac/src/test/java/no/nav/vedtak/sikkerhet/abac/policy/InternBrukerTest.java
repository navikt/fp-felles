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
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

@ExtendWith(MockitoExtension.class)
class InternBrukerTest {

    @Test
    void tilgang_applikasjon_read() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.READ, ResourceType.APPLIKASJON);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void avslag_applikasjon_read_med_personer() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ, ResourceType.APPLIKASJON);
        var ressursdata = AppRessursData.builder().leggTilAktørId("1234567890123").build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressursdata);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_applikasjon_create() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.CREATE, ResourceType.APPLIKASJON);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tilgang_drift_read() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ, ResourceType.DRIFT);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.DRIFT)).isTrue();
    }

    @Test
    void tilgang_drift_create() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.CREATE, ResourceType.DRIFT);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.DRIFT)).isTrue();
    }

    @Test
    void avslå_drift_update() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.UPDATE, ResourceType.DRIFT);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslå_ventefrist_read() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.CREATE, ResourceType.VENTEFRIST);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tilgang_ventefrist_update_veileder() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.UPDATE, ResourceType.VENTEFRIST);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_ventefrist_update_saksbehandler() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE, ResourceType.VENTEFRIST);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_risiko_read() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.READ, ResourceType.RISIKOKLASSIFISERING);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void tilgang_risiko_create() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.CREATE, ResourceType.RISIKOKLASSIFISERING);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_risiko_update() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.UPDATE, ResourceType.RISIKOKLASSIFISERING);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().isEmpty()).isTrue();
    }

    @Test
    void tilgang_oppgavestyrer_read() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.READ, ResourceType.OPPGAVESTYRING);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OPPGAVESTYRER)).isTrue();
    }

    @Test
    void tilgang_oppgavestyrer_update() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.UPDATE, ResourceType.OPPGAVESTYRING);

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OPPGAVESTYRER)).isTrue();
    }


    @Test
    void tilgang_avdelingenhet_create() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.CREATE, ResourceType.OPPGAVESTYRING_AVDELINGENHET);
        var ressurs = AppRessursData.builder().leggTilRessurs(ForeldrepengerDataKeys.AVDELING_ENHET, "4867").build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OPPGAVESTYRER)).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.STRENGTFORTROLIG)).isFalse();
    }

    @Test
    void tilgang_avdelingenhet_delete() {
        var attributter = lagAttributter(AnsattGruppe.VEILEDER, ActionType.DELETE, ResourceType.OPPGAVESTYRING_AVDELINGENHET);
        var ressurs = AppRessursData.builder().leggTilRessurs(ForeldrepengerDataKeys.AVDELING_ENHET, "4867").build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);
        assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OPPGAVESTYRER)).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.STRENGTFORTROLIG)).isFalse();
    }

    @Test
    void tilgang_avdelingenhet_update_sf() {
        var attributter = lagAttributter(AnsattGruppe.SAKSBEHANDLER, ActionType.UPDATE, ResourceType.OPPGAVESTYRING_AVDELINGENHET);
        var ressurs = AppRessursData.builder().leggTilRessurs(ForeldrepengerDataKeys.AVDELING_ENHET, "2103").build();

        var resultat = InternBrukerPolicies.vurderTilgang(attributter, ressurs);assertThat(resultat.fikkTilgang()).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.OPPGAVESTYRER)).isTrue();
        assertThat(resultat.kreverGrupper().contains(AnsattGruppe.STRENGTFORTROLIG)).isTrue();
    }


    private BeskyttetRessursAttributter lagAttributter(AnsattGruppe ansattGruppe, ActionType actionType, ResourceType resourceType) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId("12345678901")
            .medBrukerOid(UUID.randomUUID())
            .medIdentType(IdentType.InternBruker)
            .medAnsattGrupper(Set.of(ansattGruppe))
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(resourceType)
            .medActionType(actionType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
