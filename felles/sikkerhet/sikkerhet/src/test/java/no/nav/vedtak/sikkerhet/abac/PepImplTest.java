package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.context.SubjectHandlerUtils;

public class PepImplTest {

    private PepImpl pep;
    private PdpKlient pdpKlientMock;

    @BeforeEach
    public void setUp() {
        pdpKlientMock = mock(PdpKlient.class);
        pep = new PepImpl(pdpKlientMock, new DummyRequestBuilder(), new DefaultAbacSporingslogg(), "SRVFPLOS,SRVPDP");
    }

    @AfterEach
    public void clearSubjectHandler() {
        SubjectHandlerUtils.reset();
    }

    @Test
    public void skal_gi_tilgang_til_srvpdp_for_piptjeneste() {
        SubjectHandlerUtils.setInternBruker("srvpdp");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.PIP.getEksternKode())
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isTrue();
        verifyZeroInteractions(pdpKlientMock);
    }

    @Test
    public void skal_nekte_tilgang_til_saksbehandler_for_piptjeneste() {
        SubjectHandlerUtils.setInternBruker("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.PIP.getEksternKode())
                .setAction("READ");

        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        assertThat(permit.fikkTilgang()).isFalse();
        verifyZeroInteractions(pdpKlientMock);
    }

    @Test
    public void skal_kalle_pdp_for_annet_enn_pip_tjenester() {
        SubjectHandlerUtils.setInternBruker("z142443");
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy")
                .setResource(BeskyttetRessursResourceAttributt.FAGSAK.getEksternKode())
                .setAction("READ");

        @SuppressWarnings("unused")
        Tilgangsbeslutning permit = pep.vurderTilgang(attributter);
        verify(pdpKlientMock, times(1)).forespørTilgang(any(PdpRequest.class));
    }
}
