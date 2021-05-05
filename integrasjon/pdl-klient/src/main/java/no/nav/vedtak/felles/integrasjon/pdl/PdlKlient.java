package no.nav.vedtak.felles.integrasjon.pdl;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NOT_MODIFIED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
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
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.ObjectReaderResponseHandler;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class PdlKlient implements Pdl {
    private static final ObjectMapper MAPPER = mapper();
    @Deprecated(forRemoval = true, since = "3.0.x")
    public static final String PDL_KLIENT_NOT_FOUND_KODE = Pdl.PDL_KLIENT_NOT_FOUND_KODE;
    private static final List<Integer> HTTP_KODER_TOM_RESPONS = List.of(SC_NOT_MODIFIED, SC_NO_CONTENT, SC_ACCEPTED);

    private URI endpoint;
    private CloseableHttpClient restKlient;
    private GraphQLErrorHandler errorHandler;
    private String tema;

    PdlKlient() {
    }

    @Inject
    public PdlKlient(@KonfigVerdi(value = "pdl.base.url", defaultVerdi = "http://pdl-api.pdl/graphql") URI endpoint,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = "FOR") String tema,
            StsAccessTokenConfig config) {
        this(endpoint, tema, new SystemConsumerStsRestClient(config), new PdlDefaultErrorHandler());
    }

    PdlKlient(URI endpoint, String tema, CloseableHttpClient klient, GraphQLErrorHandler errorHandler) {
        this.endpoint = validate(endpoint);
        this.restKlient = klient;
        this.errorHandler = errorHandler;
        this.tema = tema;
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

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(q, p), clazz);

    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        T res = spør(post(req), new ObjectReaderResponseHandler<T>(endpoint, MAPPER.readerFor(clazz)));
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), endpoint, PDL_ERROR_RESPONSE);
        }
        return res;
    }

    private HttpPost post(GraphQLRequest req) {
        try {
            var post = new HttpPost(endpoint);
            post.setEntity(new StringEntity(req.toHttpJsonBody()));
            post.setHeader("TEMA", tema);
            return post;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T extends GraphQLResult<?>> T spør(HttpPost req, ObjectReaderResponseHandler<T> responseHandler) {
        try (var res = restKlient.execute(req)) {
            var status = res.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return responseHandler.handleResponse(res);
            }
            var body = HTTP_KODER_TOM_RESPONS.contains(status)
                    ? "<tom_respons>"
                    : EntityUtils.toString(res.getEntity());
            var msg = "Kunne ikke hente informasjon for query mot PDL: " + req.getURI()
                    + ", HTTP request=" + req.getEntity()
                    + ", HTTP status=" + res.getStatusLine()
                    + ". HTTP Errormessage=" + body;
            throw new IntegrasjonException(PDL_ERROR_RESPONSE, msg);
        } catch (IOException e) {
            throw new IntegrasjonException(PDL_IO_EXCEPTION, "IO-exception", e);
        }
    }

    private static ObjectMapper mapper() {
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
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", restKlient=" + restKlient + ", errorHandler=" + errorHandler + "]";
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p, boolean ignoreNotFound) {
        try {
            return hentPerson(q, p);
        } catch (PdlException e) {
            if (e.getStatus() == SC_NOT_FOUND && ignoreNotFound) {
                return null;
            }
            throw e;
        }
    }

}
