package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken.TokenType;

@ExtendWith(MockitoExtension.class)
class PepImplTest {

    private PepImpl pep;
    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private PdpKlient pdpKlientMock;
    @Mock
    private PdpRequestBuilder pdpRequestBuilder;

    @BeforeEach
    void setUp() {
        pep = new PepImpl(pdpKlientMock,
            tokenProvider,
            pdpRequestBuilder,
            mock(AbacAuditlogger.class),
            "SRVFPLOS,SRVPDP");
    }

    @Test
    void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("srvpdp");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
                .setAction("READ");

        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(new PdpRequest());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        when(tokenProvider.getUid()).thenReturn("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
                .setAction("READ");

        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(new PdpRequest());

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("no.nav.abac.attributter.foreldrepenger.fagsak")
                .setAction("READ");

        when(pdpRequestBuilder.lagPdpRequest(attributter))
            .thenReturn(new PdpRequest());

        @SuppressWarnings("unused")
        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock).forespørTilgang(any(PdpRequest.class));
    }
}
