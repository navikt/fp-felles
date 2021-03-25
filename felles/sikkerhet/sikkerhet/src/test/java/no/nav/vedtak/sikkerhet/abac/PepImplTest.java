package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.AbacIdToken.TokenType;

public class PepImplTest {

    private PepImpl pep;
    private TokenProvider provider;
    private PdpKlient pdpKlientMock;

    @BeforeEach
    public void setUp() {
        pdpKlientMock = mock(PdpKlient.class);
        provider = mock(TokenProvider.class);
        pep = new PepImpl(pdpKlientMock, provider, new DummyRequestBuilder(), new DefaultAbacSporingslogg(), "SRVFPLOS,SRVPDP");
    }

    @Test
    public void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {
        when(provider.getUid()).thenReturn("srvpdp");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    public void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        when(provider.getUid()).thenReturn("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyNoInteractions(pdpKlientMock);
    }

    @Test
    public void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        when(provider.getUid()).thenReturn("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy", TokenType.OIDC)
                .setResource("no.nav.abac.attributter.foreldrepenger.fagsak")
                .setAction("READ");

        @SuppressWarnings("unused")
        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock, times(1)).forespørTilgang(any(PdpRequest.class));
    }
}
