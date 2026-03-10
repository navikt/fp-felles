package no.nav.vedtak.server.rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

/*
 * Skal alltid med i Forvaltningsgrensesnitt som tilbys via swagger
 */
@Priority(Priorities.AUTHORIZATION)
public class ForvaltningAuthorizationFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext req) {
        if (!KontekstHolder.harKontekst() || !KontekstHolder.getKontekst().erAutentisert()) {
            throw new ManglerTilgangException("FORVALTNING", "Ikke autentisert");
        }
        if (!(KontekstHolder.getKontekst() instanceof RequestKontekst kontekst && kontekst.harGruppe(AnsattGruppe.DRIFT))) {
            throw new ManglerTilgangException("FORVALTNING", "Mangler driftstilgang");
        }
    }

}
