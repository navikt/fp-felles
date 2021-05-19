package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXRequestFilter;

@Dependent
@Jersey("onbehalf")
public class OnBehalfOfJerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public OnBehalfOfJerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        this(endpoint, new TokenXRequestFilter(tema));
    }

    OnBehalfOfJerseyPdlKlient(URI endpoint, ClientRequestFilter filter) {
        super(endpoint, filter);
    }

}
