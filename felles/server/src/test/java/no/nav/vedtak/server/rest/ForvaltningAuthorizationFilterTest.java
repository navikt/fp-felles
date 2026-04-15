package no.nav.vedtak.server.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

class ForvaltningAuthorizationFilterTest {

    @Test
    void ansattMedDriftrolleSkalFåTilgang() {
        var kontekst = mock(RequestKontekst.class);
        when(kontekst.harKontekst()).thenReturn(true);
        when(kontekst.erAutentisert()).thenReturn(true);
        when(kontekst.getIdentType()).thenReturn(IdentType.InternBruker);
        when(kontekst.harGruppe(AnsattGruppe.DRIFT)).thenReturn(true);
        KontekstHolder.setKontekst(kontekst);
        var forvaltningFilter = new ForvaltningAuthorizationFilter();
        Assertions.assertDoesNotThrow(() -> forvaltningFilter.filter(null));
    }

    @Test
    void ansattMedSaksbehandlerRolleIKKESkalFåTilgang() {
        var kontekst = mock(RequestKontekst.class);
        when(kontekst.erAutentisert()).thenReturn(true);
        when(kontekst.harKontekst()).thenReturn(true);
        when(kontekst.getIdentType()).thenReturn(IdentType.InternBruker);
        KontekstHolder.setKontekst(kontekst);
        var forvaltningFilter = new ForvaltningAuthorizationFilter();
        assertThatThrownBy(() -> forvaltningFilter.filter(null)).isExactlyInstanceOf(ManglerTilgangException.class);
    }

    @Test
    void eksternBrukerSkalIkkeFåTilgangTilDriftRessurser() {
        var kontekst = mock(Kontekst.class);
        when(kontekst.harKontekst()).thenReturn(true);
        when(kontekst.erAutentisert()).thenReturn(true);
        when(kontekst.getIdentType()).thenReturn(IdentType.EksternBruker);
        KontekstHolder.setKontekst(kontekst);
        var forvaltningFilter = new ForvaltningAuthorizationFilter();
        assertThatThrownBy(() -> forvaltningFilter.filter(null)).isExactlyInstanceOf(ManglerTilgangException.class);
    }
}
