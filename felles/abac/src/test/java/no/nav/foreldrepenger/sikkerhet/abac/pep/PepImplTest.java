package no.nav.foreldrepenger.sikkerhet.abac.pep;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.sikkerhet.abac.PdpRequestBuilder;
import no.nav.foreldrepenger.sikkerhet.abac.auditlog.AbacAuditlogger;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.Pdp;
import no.nav.vedtak.log.audit.Auditlogger;

@ExtendWith(MockitoExtension.class)
class PepImplTest {

    private PepImpl pep;
    @Mock
    private Pdp pdpKlientMock;
    @Mock
    private Pdp nyPdpKlientMock;
    @Mock
    private PdpRequestBuilder pdpRequestBuilder;

    @BeforeEach
    void setUp() {
        pep = new PepImpl(
            pdpKlientMock,
            nyPdpKlientMock,
            pdpRequestBuilder,
            new AbacAuditlogger(new Auditlogger(true, "felles", "felles-test")),
            "SRVFPLOS,SRVPDP");
    }

    @Test
    void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {

        BeskyttRessursAttributer attributter = new BeskyttRessursAttributer()
            .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
            .setActionType(ActionType.READ)
            .setRequestPath("/test/path");
        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(buildPdpRequest(attributter, "srvpdp"));

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        BeskyttRessursAttributer attributter = new BeskyttRessursAttributer()
            .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
            .setActionType(ActionType.READ)
            .setRequestPath("/test/path");

        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(buildPdpRequest(attributter, "saksbehandler"));


        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        BeskyttRessursAttributer attributter = new BeskyttRessursAttributer()
            .setResource("no.nav.abac.attributter.foreldrepenger.fagsak")
            .setActionType(ActionType.READ)
            .setRequestPath("/test/path");

        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(buildPdpRequest(attributter, "saksbehandler"));

        @SuppressWarnings("unused")
        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock).forespørTilgang(any(PdpRequest.class));
    }

    private PdpRequest buildPdpRequest(BeskyttRessursAttributer attributter, String userId) {
        return PdpRequest.builder()
            .medDomene("testDomene")
            .medActionType(attributter.getActionType())
            .medResourceType(attributter.getResource())
            .medRequest(attributter.getRequestPath())
            .medUserId(userId)
            .medIdToken(IdToken.withToken("testTokenString", TokenType.OIDC))
            .build();
    }
}
