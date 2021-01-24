package no.nav.vedtak.felles.integrasjon.saf;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.saf.SafTjeneste.SafTjenesteFeil.FEILFACTORY;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectReader;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktFagsakQueryResponse;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostQueryResponse;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.saf.TilknyttedeJournalposterQueryRequest;
import no.nav.saf.TilknyttedeJournalposterQueryResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLDefaultErrorHandler;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler;
import no.nav.vedtak.konfig.KonfigVerdi;

/**
 *
 * @deprecated Denne koden er en katastrofe, bruk {@link JerseySafTjeneste}
 */
@Deprecated
@Dependent
public class SafTjeneste implements Saf {

    private static List<Integer> HTTP_KODER_TOM_RESPONS = List.of(
            HttpStatus.SC_NOT_MODIFIED,
            HttpStatus.SC_NO_CONTENT,
            HttpStatus.SC_ACCEPTED);

    private URI graphqlEndpoint;
    private URI hentDokumentEndpoint;
    private CloseableHttpClient restKlient;
    private final ObjectReader objectReaderJournalpostResponse = mapper.readerFor(JournalpostQueryResponse.class);
    private final ObjectReader objectReaderDokumentoversiktFagsakResponse = mapper.readerFor(DokumentoversiktFagsakQueryResponse.class);
    private final ObjectReader objectReaderTilknyttedeJournalposteResponse = mapper.readerFor(TilknyttedeJournalposterQueryResponse.class);

    private GraphQLErrorHandler errorHandler;

    SafTjeneste() {
        // CDI
    }

    @Inject
    public SafTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = "https://localhost:8063/rest/api/saf") URI endpoint,
            OidcRestClient restKlient) {
        this.graphqlEndpoint = URI.create(endpoint.toString() + "/graphql");
        this.hentDokumentEndpoint = URI.create(endpoint.toString() + "/rest/hentdokument");
        this.restKlient = restKlient;
        this.errorHandler = new GraphQLDefaultErrorHandler();
    }

    @Override
    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest query, DokumentoversiktResponseProjection projection) {
        GraphQLRequest graphQLRequest = new GraphQLRequest(query, projection);

        DokumentoversiktFagsakQueryResponse graphQlResponse = query(graphQLRequest, objectReaderDokumentoversiktFagsakResponse);

        return graphQlResponse.dokumentoversiktFagsak();
    }

    @Override
    public Journalpost hentJournalpostInfo(JournalpostQueryRequest query, JournalpostResponseProjection projection) {
        GraphQLRequest graphQLRequest = new GraphQLRequest(query, projection);

        JournalpostQueryResponse graphQlResponse = query(graphQLRequest, objectReaderJournalpostResponse);

        return graphQlResponse.journalpost();
    }

    @Override
    public List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest query, JournalpostResponseProjection projection) {
        GraphQLRequest graphQLRequest = new GraphQLRequest(query, projection);

        TilknyttedeJournalposterQueryResponse graphQlResponse = query(graphQLRequest, objectReaderTilknyttedeJournalposteResponse);

        return graphQlResponse.tilknyttedeJournalposter();
    }

    @Override
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

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(q, p), mapper.readerFor(clazz));
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest request, ObjectReader objectReader) {
        var responseHandler = new OidcRestClientResponseHandler.ObjectReaderResponseHandler<T>(graphqlEndpoint, objectReader);

        T res;
        try {
            var httpPost = new HttpPost(graphqlEndpoint);
            httpPost.setEntity(new StringEntity(request.toHttpJsonBody()));
            res = utførForespørsel(httpPost, responseHandler);
        } catch (Exception e) {
            throw FEILFACTORY.safForespørselFeilet(request.toQueryString(), e).toException();
        }
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), graphqlEndpoint, "F-588730");
        }
        return res;
    }

    private <T extends GraphQLResult<?>> T utførForespørsel(HttpPost request,
            OidcRestClientResponseHandler.ObjectReaderResponseHandler<T> responseHandler) throws IOException {
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
                throw new IntegrasjonException(feilmelding);
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
                throw new IntegrasjonException(feilmelding);
            }
        }
    }

    interface SafTjenesteFeil extends DeklarerteFeil { // NOSONAR - internt interface er ok her
        SafTjenesteFeil FEILFACTORY = FeilFactory.create(SafTjenesteFeil.class); // NOSONAR ok med konstant

        @TekniskFeil(feilkode = "F-240613", feilmelding = "Forespørsel til SAF feilet for spørring %s", logLevel = LogLevel.WARN)
        Feil safForespørselFeilet(String query, Throwable t);

        @TekniskFeil(feilkode = "F-588730", feilmelding = "Feil fra SAF ved utført query. Error: %s", logLevel = LogLevel.WARN)
        Feil forespørselReturnerteFeil(String response);
    }

}
