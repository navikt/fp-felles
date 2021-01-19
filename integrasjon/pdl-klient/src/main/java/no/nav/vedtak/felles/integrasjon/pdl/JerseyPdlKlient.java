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
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentGeografiskTilknytningQueryResponse;
import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkQueryResponse;
import no.nav.pdl.HentIdenterBolkResult;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentIdenterQueryResponse;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.HentPersonQueryResponse;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Alternative
public class JerseyPdlKlient extends AbstractJerseyOidcRestClient implements Pdl {
    private static final Logger LOG = LoggerFactory.getLogger(JerseyPdlKlient.class);
    private URI endpoint;
    private PdlErrorHandler errorHandler;

    JerseyPdlKlient() {
    }

    @Inject
    public JerseyPdlKlient(@KonfigVerdi(value = "pdl.base.url", defaultVerdi = "http://pdl-api.default/graphql") URI endpoint,
            StsAccessTokenConfig config, PdlErrorHandler errorHandler, @KonfigVerdi(value = "pdl.tema", defaultVerdi = "FOR") String tema) {
        this(endpoint, errorHandler, new StsAccessTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
    }

    JerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        this(endpoint, new PdlDefaultErrorHandler(), filters);
    }

    JerseyPdlKlient(URI endpoint, PdlErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(mapper(), filters);
        this.endpoint = validate(endpoint);
        this.errorHandler = errorHandler;
    }

    private static URI validate(URI endpoint) {
        if (!endpoint.toString().endsWith("graphql")) {
            throw new IllegalArgumentException(
                    "Ekplisitt konfigurert URL fra property pdl.base.url må inneholde path /graphql, verdi er " + endpoint);
        }
        return endpoint;
    }

    @Override
    public GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p) {
        return query(new GraphQLRequest(q, p), HentGeografiskTilknytningQueryResponse.class).hentGeografiskTilknytning();
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p) {
        return query(new GraphQLRequest(q, p), HentPersonQueryResponse.class).hentPerson();
    }

    @Override
    public Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p) {
        return query(new GraphQLRequest(q, p), HentIdenterQueryResponse.class).hentIdenter();
    }

    @Override
    public List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p) {
        return query(new GraphQLRequest(q, p), HentIdenterBolkQueryResponse.class).hentIdenterBolk();
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
