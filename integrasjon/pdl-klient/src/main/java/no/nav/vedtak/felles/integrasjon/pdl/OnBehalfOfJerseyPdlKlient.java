package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXRequestFilter;

@Dependent
@Jersey("onbehalf")
public class OnBehalfOfJerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public OnBehalfOfJerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        this(endpoint, new PdlDefaultErrorHandler(), new TokenXRequestFilter(tema));
    }

    protected OnBehalfOfJerseyPdlKlient(GraphQLErrorHandler errorHandler) {
        this(URI.create(HTTP_PDL_API_DEFAULT_GRAPHQL), errorHandler, FOR);
    }

    protected OnBehalfOfJerseyPdlKlient(URI endpoint, GraphQLErrorHandler errorHandler, String tema) {
        this(endpoint, errorHandler, new TokenXRequestFilter(tema));
    }

    protected OnBehalfOfJerseyPdlKlient(URI endpoint, GraphQLErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(endpoint, errorHandler, filters);
    }

    protected OnBehalfOfJerseyPdlKlient(URI uri, ClientRequestFilter... filters) {
        this(uri, new PdlDefaultErrorHandler(), filters);
    }
}
