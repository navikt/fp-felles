package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenProvider;
import no.nav.vedtak.felles.integrasjon.rest.jersey.SystemTokenClientRequestFilter;

@Dependent
@Jersey("system")
public class SystemJerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public SystemJerseyPdlKlient(
        @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
        @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema,
        StsAccessTokenProvider provider) {
        this(endpoint, new SystemTokenClientRequestFilter(provider, tema));
    }

    protected SystemJerseyPdlKlient(GraphQLErrorHandler errorHandler) {
        this(URI.create(HTTP_PDL_API_DEFAULT_GRAPHQL), errorHandler);
    }

    protected SystemJerseyPdlKlient(URI uri, ClientRequestFilter... filters) {
        this(uri, new PdlDefaultErrorHandler(), filters);
    }

    protected SystemJerseyPdlKlient(URI endpoint, GraphQLErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(endpoint, errorHandler, filters);
    }
}
