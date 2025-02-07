package no.nav.vedtak.sikkerhet.abac.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.konfig.Namespace;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.Token;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursInterceptorTest;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;

@ExtendWith(MockitoExtension.class)
class SystemressursTest {

    private static final String LOCAL_APP = "vtp:" + Namespace.foreldrepenger().getName() + ":application";

    @BeforeAll
    static void initEnv() {
        System.setProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name(), LOCAL_APP + ", vtp:annetnamespace:eksternapplication");
    }

    @AfterAll
    static void avsluttEnv() {
        System.clearProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name());
    }


    @Test
    void skal_gi_tilgang_for_intern_azure_cc() {
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, LOCAL_APP);

        var resultat = SystemressursPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void skal_gi_avslag_for_ekstern_azure_cc() {
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, "vtp:annetnamespace:ukjentapplication");

        var resultat = SystemressursPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    @Test
    void skal_gi_avslag_for_godkjent_ekstern_azure_cc_men_i_feil_klusterklasse() {
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, "vtp:annetnamespace:eksternapplication");

        var resultat = SystemressursPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isFalse();
    }


    @Test
    void skal_gi_tilgang_for_godkjent_ekstern_azure_cc() {
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.ALL, "vtp:annetnamespace:eksternapplication");

        var resultat = SystemressursPolicies.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void godta_fagsak_update_med_beh_status() {
        var attributter = lagBeskyttetRessursAttributter();
        var appressursData = AppRessursData.builder().medBehandlingStatus(PipBehandlingStatus.UTREDES).build();

        var resultat = SystemressursPolicies.vurderTilgang(attributter, appressursData);
        assertThat(resultat.fikkTilgang()).isTrue();
    }

    @Test
    void avslå_fagsak_update_uten_beh_status() {
        var attributter = lagBeskyttetRessursAttributter();
        var appressursData = AppRessursData.builder().build();

        var resultat = SystemressursPolicies.vurderTilgang(attributter, appressursData);
        assertThat(resultat.fikkTilgang()).isFalse();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributter() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(LOCAL_APP)
            .medIdentType(IdentType.Systemressurs)
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(ResourceType.FAGSAK)
            .medActionType(ActionType.UPDATE)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterAzure(AvailabilityType availabilityType, String brukerId) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(brukerId)
            .medIdentType(IdentType.Systemressurs)
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(ResourceType.APPLIKASJON)
            .medActionType(ActionType.READ)
            .medAvailabilityType(availabilityType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
