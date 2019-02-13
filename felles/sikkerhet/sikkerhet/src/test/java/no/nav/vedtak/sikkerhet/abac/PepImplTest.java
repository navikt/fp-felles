package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;

public class PepImplTest {

    private PepImpl pep;
    private PdpKlient pdpKlientMock;


    @Before
    public void setUp() {
        pdpKlientMock = mock(PdpKlient.class);
        pep = new PepImpl(pdpKlientMock, new DummyRequestBuilder());
    }

    @After
    public void clearSubjectHandler() {
        SubjectHandlerUtils.reset();
    }

    @Test
    public void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {
        SubjectHandlerUtils.setInternBruker("srvPDP");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.PIP)
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyZeroInteractions(pdpKlientMock);
    }

    @Test
    public void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        SubjectHandlerUtils.setInternBruker("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.PIP)
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyZeroInteractions(pdpKlientMock);
    }

    @Test
    public void skal_kalle_pdp_for_annet_enn_pip_tjenester(){
        SubjectHandlerUtils.setInternBruker("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.FAGSAK)
                .setAction("READ");

        @SuppressWarnings("unused")
        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock, times(1)).forespørTilgang(any(PdpRequest.class));
    }
}