package no.nav.vedtak.felles.integrasjon.pdl;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
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
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.ObjectReaderResponseHandler;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class PdlKlient implements PDL {
    private static final String PDL_KLIENT_NOT_FOUND_KODE = "F-399736";
    private static final ObjectMapper MAPPER = mapper();

    private static final List<Integer> HTTP_KODER_TOM_RESPONS = List.of(SC_NOT_MODIFIED, SC_NO_CONTENT, SC_ACCEPTED);

    private URI endpoint;
    private CloseableHttpClient restKlient;
    private PdlErrorHandler errorHandler;

    PdlKlient() {
    }

    @Inject
    public PdlKlient(@KonfigVerdi(value = "pdl.base.url", defaultVerdi = "https://localhost:8063/rest/api/pdl") URI endpoint,
            StsAccessTokenConfig config, PdlErrorHandler errorHandler) {
        this(endpoint, new SystemConsumerStsRestClient(config), errorHandler);
    }

    PdlKlient(URI endpoint, CloseableHttpClient klient, PdlErrorHandler errorHandler) {
        this.endpoint = URI.create(endpoint.toString() + "/graphql");
        this.restKlient = klient;
        this.errorHandler = errorHandler;
    }

    @Override
    public GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p, Tema tema) {
        return query(new GraphQLRequest(q, p), HentGeografiskTilknytningQueryResponse.class, tema).hentGeografiskTilknytning();
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p, Tema tema) {
        return query(new GraphQLRequest(q, p), HentPersonQueryResponse.class, tema).hentPerson();
    }

    @Override
    public Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p, Tema tema) {
        return query(new GraphQLRequest(q, p), HentIdenterQueryResponse.class, tema).hentIdenter();
    }

    @Override
    public List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p, Tema tema) {
        return query(new GraphQLRequest(q, p), HentIdenterBolkQueryResponse.class, tema).hentIdenterBolk();
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz, Tema tema) {
        T res = spør(post(req, tema), new ObjectReaderResponseHandler<T>(endpoint, MAPPER.readerFor(clazz)));
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors());
        }
        return res;
    }

    private HttpPost post(GraphQLRequest req, Tema tema) {
        try {
            var post = new HttpPost(endpoint);
            post.setEntity(new StringEntity(req.toHttpJsonBody()));
            post.setHeader("TEMA", tema.name());
            return post;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <T extends GraphQLResult<?>> T spør(HttpPost req, ObjectReaderResponseHandler<T> responseHandler) {
        try (var httpResponse = restKlient.execute(req)) {
            var responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                return responseHandler.handleResponse(httpResponse);
            }
            var responseBody = HTTP_KODER_TOM_RESPONS.contains(responseCode)
                    ? "<tom_respons>"
                    : EntityUtils.toString(httpResponse.getEntity());
            var feilmelding = "Kunne ikke hente informasjon for query mot PDL: " + req.getURI()
                    + ", HTTP request=" + req.getEntity()
                    + ", HTTP status=" + httpResponse.getStatusLine()
                    + ". HTTP Errormessage=" + responseBody;
            throw new RuntimeException(feilmelding);
        } catch (IOException e) {
            throw PdlTjenesteFeil.FEILFACTORY.pdlForespørselFeilet(endpoint.toString(), e).toException();
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

    interface PdlTjenesteFeil extends DeklarerteFeil { // NOSONAR - internt interface er ok her
        PdlTjenesteFeil FEILFACTORY = FeilFactory.create(PdlTjenesteFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "F-539237", feilmelding = "Forespørsel til PDL feilet for spørring %s", logLevel = LogLevel.WARN)
        Feil pdlForespørselFeilet(String query, Throwable t);

        @TekniskFeil(feilkode = "F-399735", feilmelding = "Feil fra PDL ved utført query. Error: %s", logLevel = LogLevel.WARN)
        Feil forespørselReturnerteFeil(String response);

        @FunksjonellFeil(feilkode = PDL_KLIENT_NOT_FOUND_KODE, feilmelding = "Feil fra PDL ved utført query. Error: Person ikke funnet", løsningsforslag = "Slå opp gyldig ident", logLevel = LogLevel.WARN)
        Feil personIkkeFunnet();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", restKlient=" + restKlient + ", errorHandler=" + errorHandler + "]";
    }
}
