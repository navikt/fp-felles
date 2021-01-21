package no.nav.vedtak.felles.integrasjon.pdl;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;

public abstract class AbstractJerseyPdlKlient extends AbstractJerseyRestClient implements PDLQueryable {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJerseyPdlKlient.class);
    protected static final String HTTP_PDL_API_DEFAULT_GRAPHQL = "http://pdl-api.default/graphql";
    protected static final String FOR = "FOR";

    private final URI endpoint;
    private final PdlErrorHandler errorHandler;

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config) {
        this(config, FOR);
    }

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config, URI endpoint) {
        this(config, endpoint, FOR);
    }

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config, String tema) {
        this(config, HTTP_PDL_API_DEFAULT_GRAPHQL, tema);
    }

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config, String endpoint, String tema) {
        this(config, URI.create(endpoint), tema);
    }

    AbstractJerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        this(endpoint, new PdlDefaultErrorHandler(), filters);
    }

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config, URI endpoint, String tema) {
        this(endpoint, new PdlDefaultErrorHandler(), new StsAccessTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
    }

    AbstractJerseyPdlKlient(URI endpoint, PdlErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
        this.errorHandler = errorHandler;
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(q, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        try {
            LOG.info("Henter resultat for {}", clazz.getName());
            var res = client.target(endpoint)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz);
            if (res.hasErrors()) {
                return errorHandler.handleError(res.getErrors(), endpoint);
            }
            return res;
        } catch (ProcessingException e) {
            if (e.getCause() != null && e.getCause() instanceof VLException) {
                throw VLException.class.cast(e.getCause());
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", errorHandler=" + errorHandler + "]";
    }
}
