package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.konfig.Namespace;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursInterceptorTest;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

@ExtendWith(MockitoExtension.class)
class PepImplTest {

    private static final String LOCAL_APP = "local:" + Namespace.foreldrepenger().getName() + ":application";

    private PepImpl pep;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private PdpKlient pdpKlientMock;
    @Mock
    private PdpRequestBuilder pdpRequestBuilder;

    @BeforeAll
    static void initEnv() {
        System.setProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name(), LOCAL_APP + ", local:annetnamespace:eksternapplication");
    }

    @AfterAll
    static void avsluttEnv() {
        System.clearProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name());
    }

    @BeforeEach
    void setUp() {
        pep = new PepImpl(pdpKlientMock, tokenProvider, pdpRequestBuilder, "SRVFPLOS,SRVPDP");
    }

    @Test
    void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("srvpdp");
        var attributter = lagBeskyttetRessursAttributterPip();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributterPip();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_gi_tilgang_for_intern_azure_cc() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn(LOCAL_APP);
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, token, LOCAL_APP, IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_gi_avslag_for_ekstern_azure_cc() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("local:annetnamespace:ukjentapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, token, "local:annetnamespace:ukjentapplication",
            IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_gi_tilgang_for_godkjent_ekstern_azure_cc() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("local:annetnamespace:eksternapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.ALL, token, "local:annetnamespace:eksternapplication",
            IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_sjekke_mot_abac_hvis_sts_systembruker() {
        var token = new OpenIDToken(OpenIDProvider.STS, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("srvTestbruker");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.ALL, token, "srvTestbruker", IdentType.Systemressurs);

        when(pdpRequestBuilder.abacDomene()).thenReturn("domene");
        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        pep.vurderTilgang(attributter);
        verify(pdpKlientMock, times(1)).forespørTilgang(eq(attributter), eq("domene"), any());
    }

    @Test
    void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributter();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());
        when(pdpRequestBuilder.abacDomene()).thenReturn("foreldrepenger");

        @SuppressWarnings("unused") Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock).forespørTilgang(eq(attributter), any(String.class), any(AppRessursData.class));
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributter() {
        return BeskyttetRessursAttributter.builder()
            .medUserId(tokenProvider.getUid())
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN, "TEST", IdentType.InternBruker))
            .medResourceType(ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK)
            .medActionType(ActionType.READ)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medServiceType(ServiceType.REST)
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterPip() {
        return BeskyttetRessursAttributter.builder()
            .medUserId(tokenProvider.getUid())
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN, "TEST", IdentType.InternBruker))
            .medResourceType(RESOURCE_TYPE_INTERNAL_PIP)
            .medActionType(ActionType.READ)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medServiceType(ServiceType.REST)
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterAzure(AvailabilityType availabilityType,
                                                                            OpenIDToken token,
                                                                            String brukerId,
                                                                            IdentType identType) {
        return BeskyttetRessursAttributter.builder()
            .medUserId(tokenProvider.getUid())
            .medToken(Token.withOidcToken(token, brukerId, identType))
            .medResourceType(ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK)
            .medActionType(ActionType.READ)
            .medAvailabilityType(availabilityType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medServiceType(ServiceType.REST)
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
