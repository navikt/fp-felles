package no.nav.vedtak.felles.integrasjon.saf;


import static no.nav.vedtak.felles.integrasjon.saf.SafTjeneste.SafTjenesteFeil.FEILFACTORY;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler;
import no.nav.vedtak.felles.integrasjon.saf.graphql.DokumentoversiktFagsakQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.GraphQlRequest;
import no.nav.vedtak.felles.integrasjon.saf.graphql.GraphQlResponse;
import no.nav.vedtak.felles.integrasjon.saf.graphql.HentDokumentQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.JournalpostQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.SafQuery;
import no.nav.vedtak.felles.integrasjon.saf.graphql.Variables;
import no.nav.vedtak.felles.integrasjon.saf.rest.model.DokumentoversiktFagsak;
import no.nav.vedtak.felles.integrasjon.saf.rest.model.Journalpost;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class SafTjeneste {

    private static List<Integer> HTTP_KODER_TOM_RESPONS = List.of(
        HttpStatus.SC_NOT_MODIFIED,
        HttpStatus.SC_NO_CONTENT,
        HttpStatus.SC_ACCEPTED);

    private URI graphqlEndpoint;
    private URI hentDokumentEndpoint;
    private CloseableHttpClient restKlient;

    private String journalpostQueryDef;
    private String dokumentoversiktFagsakQueryDef;

    private final ObjectMapper objectMapper = createObjectMapper();
    private final ObjectReader objectReader = objectMapper.readerFor(GraphQlResponse.class);
    private final ObjectWriter objectWriter = objectMapper.writer();

    SafTjeneste() {
        // CDI
    }

    @Inject
    public SafTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = "https://localhost:8063/rest/api/saf") URI endpoint,
                       OidcRestClient restKlient) {
        this.graphqlEndpoint = URI.create(endpoint.toString() + "/graphql");
        this.hentDokumentEndpoint = URI.create(endpoint.toString() + "/rest/hentdokument");
        this.restKlient = restKlient;

        this.journalpostQueryDef = ReadFileFromClassPathHelper.hent("saf/journalpostQuery.graphql");
        this.dokumentoversiktFagsakQueryDef = ReadFileFromClassPathHelper.hent("saf/dokumentoversiktFagsakQuery.graphql");
    }

    public DokumentoversiktFagsak dokumentoversiktFagsak(DokumentoversiktFagsakQuery query) {
        var graphQlRequest = new GraphQlRequest(dokumentoversiktFagsakQueryDef, new Variables(query.getFagsakId(), query.getFagsaksystem()));

        var graphQlResponse = utførSpørring(query, graphQlRequest);

        return ektraherDokumentoversiktFagsak(query, graphQlResponse);
    }

    public Journalpost hentJournalpostInfo(JournalpostQuery query) {
        var graphQlRequest = new GraphQlRequest(journalpostQueryDef, new Variables(query.getJournalpostId()));

        var graphQlResponse = utførSpørring(query, graphQlRequest);

        return ektraherJournalpost(query, graphQlResponse);
    }

    public byte[] hentDokument(HentDokumentQuery query) {
        var uri = URI.create(hentDokumentEndpoint.toString() +
            String.format("/%s/%s/%s", query.getJournalpostId(), query.getDokumentInfoId(), query.getVariantFormat()));
        var getRequest = new HttpGet(uri);

        try {
            return utførForespørselDokumentinnhold(getRequest);
        } catch (Exception e) {
            throw FEILFACTORY.safForespørselFeilet(query, e).toException();
        }
    }

    private GraphQlResponse utførSpørring(SafQuery query, GraphQlRequest graphQlRequest) {
        var responseHandler = new OidcRestClientResponseHandler.ObjectReaderResponseHandler<GraphQlResponse>(graphqlEndpoint, objectReader);

        GraphQlResponse graphQlResponse;
        try {
            var httpPost = new HttpPost(graphqlEndpoint);
            httpPost.setEntity(new StringEntity(objectWriter.writeValueAsString(graphQlRequest)));
            graphQlResponse = utførForespørsel(httpPost, responseHandler);
        } catch (Exception e) {
            throw FEILFACTORY.safForespørselFeilet(query, e).toException();
        }

        if (graphQlResponse.getErrors() != null && graphQlResponse.getErrors().size() > 0) {
            throw FEILFACTORY.forespørselReturnerteFeil(graphQlResponse).toException();
        }
        return graphQlResponse;
     }

    private DokumentoversiktFagsak ektraherDokumentoversiktFagsak(DokumentoversiktFagsakQuery query, GraphQlResponse graphQlResponse) {
        if (graphQlResponse.getData() == null || graphQlResponse.getData().getDokumentoversiktFagsak() == null) {
            throw FEILFACTORY.safResponsTom(query).toException();
        }
        return graphQlResponse.getData().getDokumentoversiktFagsak();
    }

    private Journalpost ektraherJournalpost(JournalpostQuery query, GraphQlResponse graphQlResponse) {
        if (graphQlResponse.getData() == null || graphQlResponse.getData().getJournalpost() == null) {
            throw FEILFACTORY.safResponsTom(query).toException();
        }
        return graphQlResponse.getData().getJournalpost();

    }

    private <T> T utførForespørsel(HttpPost request, OidcRestClientResponseHandler.ObjectReaderResponseHandler<T> responseHandler) throws IOException {
        try (var httpResponse = restKlient.execute(request)) {
            var responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                return responseHandler.handleResponse(httpResponse);
            } else {
                var responseBody = HTTP_KODER_TOM_RESPONS.contains(responseCode)
                    ? "<tom_respons>"
                    : EntityUtils.toString(httpResponse.getEntity());
                var feilmelding = "Kunne ikke hente informasjon for query mot SAF: " + request.getURI()
                    + ", HTTP request=" + request.getEntity()
                    + ", HTTP status=" + httpResponse.getStatusLine()
                    + ". HTTP Errormessage=" + responseBody;
                throw new SafException(feilmelding);
            }
        }
    }


    private byte[] utførForespørselDokumentinnhold(HttpGet request) throws IOException {
        try (var httpResponse = restKlient.execute(request)) {
            var responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                return EntityUtils.toByteArray(httpResponse.getEntity());
            } else {
                if (HTTP_KODER_TOM_RESPONS.contains(responseCode)) {
                    return null;
                }
                var responseBody = EntityUtils.toString(httpResponse.getEntity());
                var feilmelding = "Kunne ikke hente informasjon for query mot SAF: " + request.getURI()
                    + ", HTTP status=" + httpResponse.getStatusLine()
                    + ". HTTP Errormessage=" + responseBody;
                throw new SafException(feilmelding);
            }
        }
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
            .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    interface SafTjenesteFeil extends DeklarerteFeil { // NOSONAR - internt interface er ok her
        SafTjenesteFeil FEILFACTORY = FeilFactory.create(SafTjenesteFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "K9-240613", feilmelding = "Forespørsel til SAF feilet for spørring %s", logLevel = LogLevel.WARN)
        Feil safForespørselFeilet(SafQuery query, Throwable t);

        @TekniskFeil(feilkode = "K9-240614", feilmelding = "Respons fra SAF var uten innhold for spørring %s", logLevel = LogLevel.WARN)
        Feil safResponsTom(SafQuery query);

        @TekniskFeil(feilkode = "K9-588730", feilmelding = "Feil fra SAF ved utført query: %s", logLevel = LogLevel.WARN)
        Feil forespørselReturnerteFeil(GraphQlResponse response);
    }
}
