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
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktFagsakQueryResponse;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostQueryResponse;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler;
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
    private final ObjectMapper objectMapper = createObjectMapper();
    private final ObjectReader objectReaderJournalpostResponse = objectMapper.readerFor(JournalpostQueryResponse.class);
    private final ObjectReader objectReaderDokumentoversiktFagsakResponse = objectMapper.readerFor(DokumentoversiktFagsakQueryResponse.class);

    SafTjeneste() {
        // CDI
    }

    @Inject
    public SafTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = "https://localhost:8063/rest/api/saf") URI endpoint,
                       OidcRestClient restKlient) {
        this.graphqlEndpoint = URI.create(endpoint.toString() + "/graphql");
        this.hentDokumentEndpoint = URI.create(endpoint.toString() + "/rest/hentdokument");
        this.restKlient = restKlient;
    }

    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest query, DokumentoversiktResponseProjection projection) {
        GraphQLRequest graphQLRequest = new GraphQLRequest(query, projection);

        DokumentoversiktFagsakQueryResponse graphQlResponse = utførSpørring(graphQLRequest, objectReaderDokumentoversiktFagsakResponse);

        return graphQlResponse.dokumentoversiktFagsak();
    }

    public Journalpost hentJournalpostInfo(JournalpostQueryRequest query, JournalpostResponseProjection projection) {
        GraphQLRequest graphQLRequest = new GraphQLRequest(query, projection);

        JournalpostQueryResponse graphQlResponse = utførSpørring(graphQLRequest, objectReaderJournalpostResponse);

        return graphQlResponse.journalpost();
    }


    public byte[] hentDokument(HentDokumentQuery query) {
        var uri = URI.create(hentDokumentEndpoint.toString() +
            String.format("/%s/%s/%s", query.getJournalpostId(), query.getDokumentInfoId(), query.getVariantFormat()));
        var getRequest = new HttpGet(uri);

        try {
            return utførForespørselDokumentinnhold(getRequest);
        } catch (Exception e) {
            throw FEILFACTORY.safForespørselFeilet(query.toString(), e).toException();
        }
    }

    private <T extends GraphQLResult> T utførSpørring(GraphQLRequest request, ObjectReader objectReader) {
        var responseHandler = new OidcRestClientResponseHandler.ObjectReaderResponseHandler<T>(graphqlEndpoint, objectReader);

        T graphQlResponse;
        try {
            var httpPost = new HttpPost(graphqlEndpoint);
            httpPost.setEntity(new StringEntity(request.toHttpJsonBody()));
            graphQlResponse = utførForespørsel(httpPost, responseHandler);
        } catch (Exception e) {
            throw FEILFACTORY.safForespørselFeilet(request.toQueryString(), e).toException();
        }

        if (graphQlResponse.getErrors() != null && graphQlResponse.getErrors().size() > 0) {
            throw FEILFACTORY.forespørselReturnerteFeil(graphQlResponse.toString()).toException();
        }
        return graphQlResponse;
    }

    private <T extends GraphQLResult> T utførForespørsel(HttpPost request, OidcRestClientResponseHandler.ObjectReaderResponseHandler<T> responseHandler) throws IOException {
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
                //throw new SafException(feilmelding);
                throw new RuntimeException(feilmelding);
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
        Feil safForespørselFeilet(String query, Throwable t);

        @TekniskFeil(feilkode = "K9-588730", feilmelding = "Feil fra SAF ved utført query: %s", logLevel = LogLevel.WARN)
        Feil forespørselReturnerteFeil(String response);
    }
}
