package no.nav.vedtak.felles.integrasjon.pdl;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.TimeZone;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;

public abstract class AbstractJerseyPdlKlient extends AbstractJerseyRestClient {
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
        this(config, URI.create(HTTP_PDL_API_DEFAULT_GRAPHQL), tema);
    }

    public AbstractJerseyPdlKlient(StsAccessTokenConfig config, URI endpoint, String tema) {
        this(endpoint, new PdlDefaultErrorHandler(), new StsAccessTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
    }

    public AbstractJerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        this(endpoint, new PdlDefaultErrorHandler(), filters);
    }

    public AbstractJerseyPdlKlient(URI endpoint, PdlErrorHandler errorHandler, ClientRequestFilter... filters) {
        this(mapper(), endpoint, errorHandler, filters);
    }

    public AbstractJerseyPdlKlient(ObjectMapper mapper, URI endpoint, PdlErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(mapper, filters);
        this.endpoint = endpoint;
        this.errorHandler = errorHandler;
    }

    protected <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
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

    static ObjectMapper mapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
                .disable(WRITE_DATES_AS_TIMESTAMPS)
                .disable(WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(FAIL_ON_EMPTY_BEANS)
                .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
                .enable(FAIL_ON_READING_DUP_TREE_KEY)
                .enable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", errorHandler=" + errorHandler + "]";
    }
}
