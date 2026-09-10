package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
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
import no.nav.vedtak.log.mdc.LoggFelter;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;

@ExtendWith(MockitoExtension.class)
class PepImplTest {

    private static final String LOCAL_APP = "vtp:" + Namespace.foreldrepenger().getName() + ":application";

    private PepImpl pep;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private AbacAuditlogger abacAuditlogger;
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
        this.pep = new PepImpl(abacAuditlogger, popKlientMock, gruppeKlientMock, pdpRequestBuilder);
    }

    @Test
    void skal_ikke_gi_tilgang_til_srvpdp_for_piptjeneste_siden_sts_brukere_ikke_stottes_lenger() {
        when(tokenProvider.getUid()).thenReturn("srvpdp");
        var attributter = lagBeskyttetRessursAttributterPip();

        var permit = pep.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributterPip();

        var permit = pep.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_tilgang_for_intern_azure_cc() {
        when(tokenProvider.getUid()).thenReturn(LOCAL_APP);
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL, IdentType.Systemressurs);

        var ressurser = pep.hentRessurser(attributter);

        var permit = pep.vurderTilgang(attributter, ressurser);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_tilgang_for_intern_azure_cc_update() {
        when(tokenProvider.getUid()).thenReturn(LOCAL_APP);
        var attributter = lagBeskyttetRessursAttributterUpdateAzure();

        var permit = pep.vurderTilgang(attributter, AppRessursData.builder()
            .medBehandlingStatus(PipBehandlingStatus.UTREDES).medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING).build());
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_avslag_for_intern_azure_cc_update() {
        when(tokenProvider.getUid()).thenReturn(LOCAL_APP);
        var attributter = lagBeskyttetRessursAttributterUpdateAzure();

        var permit = pep.vurderTilgang(attributter, AppRessursData.builder().build());
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_avslag_for_ekstern_azure_cc() {
        when(tokenProvider.getUid()).thenReturn("vtp:annetnamespace:ukjentapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL,
            IdentType.Systemressurs);

        var ressurser = pep.hentRessurser(attributter);

        var permit = pep.vurderTilgang(attributter, ressurser);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_gi_avslag_for_godkjent_ekstern_azure_cc_men_i_feil_klusterklasse() {
        when(tokenProvider.getUid()).thenReturn("dev-fss:annetnamespace:eksternapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.INTERNAL,
            IdentType.Systemressurs);

        var ressurser = pep.hentRessurser(attributter);

        var permit = pep.vurderTilgang(attributter, ressurser);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }


    @Test
    void skal_gi_tilgang_for_godkjent_ekstern_azure_cc() {
        when(tokenProvider.getUid()).thenReturn("vtp:annetnamespace:eksternapplication");
        var attributter = lagBeskyttetRessursAttributterAzure(AvailabilityType.ALL,
            IdentType.Systemressurs);

        var ressurser = pep.hentRessurser(attributter);

        var permit = pep.vurderTilgang(attributter, ressurser);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(gruppeKlientMock);
        verifyNoInteractions(popKlientMock);
    }

    @Test
    void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        var attributter = lagBeskyttetRessursAttributter();
        var appressursData = AppRessursData.builder().leggTilIdent("1234567890123").build();

        when(popKlientMock.vurderTilgangInternBruker(any(), any(), any(), any())).thenReturn(Tilgangsvurdering.godkjenn());

        @SuppressWarnings("unused") var permit = pep.vurderTilgang(attributter, appressursData);
        verifyNoInteractions(gruppeKlientMock);
        verify(popKlientMock).vurderTilgangInternBruker(attributter.getBrukerOid(), appressursData.getIdenter(), null, null);
    }

    @Test
    void skal_legge_saksnummer_og_behandling_i_loggfelter() {
        var behandling = UUID.randomUUID();

        var appressursData = AppRessursData.builder().medSaksnummer("12345").medBehandling(behandling).build();

        assertThat(appressursData.getLoggfelter()).containsExactly(
            entry(LoggFelter.SAK, "12345"),
            entry(LoggFelter.BEHANDLING, behandling.toString()));
    }

    @Test
    void skal_kun_legge_logg_saksnummer_og_logg_behandling_i_loggfelter() {
        var behandling = UUID.randomUUID();

        var appressursData = AppRessursData.builder().medLoggSaksnummer("12345").medLoggBehandling(behandling).build();

        assertThat(appressursData.getSaksnummer()).isNull();
        assertThat(appressursData.getBehandling()).isNull();
        assertThat(appressursData.getLoggfelter()).containsExactly(
            entry(LoggFelter.SAK, "12345"),
            entry(LoggFelter.BEHANDLING, behandling.toString()));
    }

    @Test
    void skal_la_loggfelter_overstyre_saksnummer_og_behandling_for_logging() {
        var behandling = UUID.randomUUID();
        var loggBehandling = UUID.randomUUID();

        var appressursData = AppRessursData.builder()
            .medSaksnummer("12345")
            .medBehandling(behandling)
            .medLoggSaksnummer("67890")
            .medLoggBehandling(loggBehandling)
            .build();

        assertThat(appressursData.getSaksnummer()).isEqualTo("12345");
        assertThat(appressursData.getBehandling()).isEqualTo(behandling);
        assertThat(appressursData.getLoggfelter()).containsExactly(
            entry(LoggFelter.SAK, "67890"),
            entry(LoggFelter.BEHANDLING, loggBehandling.toString()));
    }

    @Test
    void skal_la_saksnummer_og_behandling_overstyre_eksisterende_loggfelter() {
        var behandling = UUID.randomUUID();

        var appressursData = AppRessursData.builder()
            .medLoggSaksnummer("67890")
            .medLoggBehandling(UUID.randomUUID())
            .medSaksnummer("12345")
            .medBehandling(behandling)
            .build();

        assertThat(appressursData.getLoggfelter()).containsExactly(
            entry(LoggFelter.SAK, "12345"),
            entry(LoggFelter.BEHANDLING, behandling.toString()));
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributter() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medBrukerOid(UUID.randomUUID())
            .medIdentType(IdentType.InternBruker)
            .medAnsattGrupper(Set.of(AnsattGruppe.SAKSBEHANDLER))
            .medResourceType(ResourceType.FAGSAK)
            .medActionType(ActionType.READ)
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterPip() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medIdentType(IdentType.InternBruker)
            .medResourceType(ResourceType.PIP)
            .medActionType(ActionType.READ)
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterAzure(AvailabilityType availabilityType, IdentType identType) {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medIdentType(identType)
            .medResourceType(ResourceType.FAGSAK)
            .medActionType(ActionType.READ)
            .medAvailabilityType(availabilityType)
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributterUpdateAzure() {
        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medIdentType(IdentType.Systemressurs)
            .medResourceType(ResourceType.FAGSAK)
            .medActionType(ActionType.UPDATE)
            .medAvailabilityType(AvailabilityType.INTERNAL)
            .medServicePath("/metode")
            .medDataAttributter(AbacDataAttributter.opprett())
            .build();
    }
}
