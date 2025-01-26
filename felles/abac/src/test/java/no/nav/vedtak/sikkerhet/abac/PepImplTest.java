package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

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
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursInterceptorTest;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonInternRequest;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;

@ExtendWith(MockitoExtension.class)
class PepImplTest {

    private static final String LOCAL_APP = "vtp:" + Namespace.foreldrepenger().getName() + ":application";

    private PepImpl pep;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private PdpKlient pdpKlientMock;
    @Mock
    private PopulasjonKlient popKlientMock;
    @Mock
    private AnsattGruppeKlient gruppeKlientMock;
    @Mock
    private PdpRequestBuilder pdpRequestBuilder;

    @BeforeAll
    static void initEnv() {
        System.setProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name(), LOCAL_APP + ", vtp:annetnamespace:eksternapplication");
    }

    @AfterAll
    static void avsluttEnv() {
        System.clearProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name());
    }

    @BeforeEach
    void setUp() {
        this.pep = new PepImpl(pdpKlientMock, popKlientMock, gruppeKlientMock, pdpRequestBuilder);
    }

    @Test
    void skal_ikke_gi_tilgang_til_srvpdp_for_piptjeneste_siden_sts_brukere_ikke_stottes_lenger() {
        when(tokenProvider.getUid()).thenReturn("srvpdp");
        var attributter = lagBeskyttetRessursAttributterPip();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributterPip();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
        verifyNoInteractions(popKlientMock);
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
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_avslag_for_ekstern_azure_cc() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("vtp:annetnamespace:ukjentapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, token, "vtp:annetnamespace:ukjentapplication",
            IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_avslag_for_godkjent_ekstern_azure_cc_men_i_feil_klusterklasse() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("dev-fss:annetnamespace:eksternapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, token, "vtp:annetnamespace:eksternapplication",
            IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
        verifyNoInteractions(popKlientMock);
    }


    @Test
    void skal_gi_tilgang_for_godkjent_ekstern_azure_cc() {
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, new TokenString("token"));
        when(tokenProvider.getUid()).thenReturn("vtp:annetnamespace:eksternapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.ALL, token, "vtp:annetnamespace:eksternapplication",
            IdentType.Systemressurs);

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(AppRessursData.builder().build());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributter();
        var appressursData = AppRessursData.builder().leggTilAktørId("1234567890123").build();

        when(pdpRequestBuilder.lagAppRessursData(any())).thenReturn(appressursData);
        when(pdpRequestBuilder.abacDomene()).thenReturn("foreldrepenger");
        when(pdpKlientMock.forespørTilgang(any(), any(), any())).thenReturn(new Tilgangsbeslutning(AbacResultat.GODKJENT, attributter, appressursData));

        @SuppressWarnings("unused") Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock).forespørTilgang(eq(attributter), any(String.class), any(AppRessursData.class));
        verify(popKlientMock).vurderTilgang(new PopulasjonInternRequest(attributter.getBrukerOid(), Set.of(), appressursData.getAktørIdSet()));
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributter() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medBrukerOid(UUID.randomUUID())
            .medIdentType(IdentType.InternBruker)
            .medAnsattGrupper(Set.of(AnsattGruppe.SAKSBEHANDLER))
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK)
            .medActionType(ActionType.READ)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterPip() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medIdentType(IdentType.InternBruker)
            .medToken(Token.withOidcToken(BeskyttetRessursInterceptorTest.DUMMY_OPENID_TOKEN))
            .medResourceType(RESOURCE_TYPE_INTERNAL_PIP)
            .medActionType(ActionType.READ)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterAzure(AvailabilityType availabilityType,
                                                                            OpenIDToken token,
                                                                            String brukerId,
                                                                            IdentType identType) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medIdentType(identType)
            .medToken(Token.withOidcToken(token))
            .medResourceType(ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK)
            .medActionType(ActionType.READ)
            .medAvailabilityType(availabilityType)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
