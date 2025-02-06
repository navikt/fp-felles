package no.nav.vedtak.sikkerhet.abac.policy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

@ExtendWith(MockitoExtension.class)
class EksternBrukerTest {


    @Test
    void tilgang_fagsak_read() {
        var attributter = lagAttributter(ActionType.READ, ResourceType.FAGSAK);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void tilgang_fagsak_create() {
        var attributter = lagAttributter(ActionType.CREATE, ResourceType.FAGSAK);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void avslag_fagsak_update() {
        var attributter = lagAttributter(ActionType.UPDATE, ResourceType.FAGSAK);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_fagsak_delete() {
        var attributter = lagAttributter(ActionType.DELETE, ResourceType.FAGSAK);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_uttaksplan_read() { // TODO - rydde, ta med ressursdata og endre til oppfylt. Nå er den ikke implementert
        var attributter = lagAttributter(ActionType.READ, ResourceType.UTTAKSPLAN);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_uttaksplan_update() { // Skal forble avslag
        var attributter = lagAttributter(ActionType.UPDATE, ResourceType.UTTAKSPLAN);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void tilgang_applikasjon_read() {
        var attributter = lagAttributter(ActionType.READ, ResourceType.APPLIKASJON);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void avslag_applikasjon_read_med_personer() {
        var attributter = lagAttributter(ActionType.READ, ResourceType.APPLIKASJON);
        var ressursdata = AppRessursData.builder().leggTilAktørId("1234567890123").build();

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, ressursdata);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void avslag_applikasjon_create() {
        var attributter = lagAttributter(ActionType.CREATE, ResourceType.APPLIKASJON);

        var resultat = EksternBrukerPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }


    private BeskyttetRessursAttributter lagAttributter(ActionType actionType, ResourceType resourceType) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId("12345678901")
            .medIdentType(IdentType.EksternBruker)
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(resourceType)
            .medActionType(actionType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
