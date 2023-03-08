package no.nav.vedtak.felles.integrasjon.saf;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

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
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

//@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "saf.base.url", endpointDefault = "https://saf.nais.adeo.no",
//    scopesProperty = "saf.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.saf/.default")
public abstract class AbstractSafKlient implements Saf {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String F_240613 = "F-240613";
    private static final String GRAPHQL_PATH = "/graphql";

    private final RestClient restKlient;
    private final RestConfig restConfig;
    private final URI graphql;
    private final SafErrorHandler errorHandler;

    protected AbstractSafKlient() {
        this(RestClient.client());
    }

    protected AbstractSafKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.graphql = URI.create(this.restConfig.endpoint() + GRAPHQL_PATH);
        this.errorHandler = new SafErrorHandler();
    }

    @Override
    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest q, DokumentoversiktResponseProjection p) {
        return query(q, p, DokumentoversiktFagsakQueryResponse.class).dokumentoversiktFagsak();
    }

    @Override
    public Journalpost hentJournalpostInfo(JournalpostQueryRequest q, JournalpostResponseProjection p) {
        return query(q, p, JournalpostQueryResponse.class).journalpost();
    }

    @Override
    public List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest q, JournalpostResponseProjection p) {
        return query(q, p, TilknyttedeJournalposterQueryResponse.class).tilknyttedeJournalposter();
    }

    @Override
    public byte[] hentDokument(HentDokumentQuery q) {
        var path = UriBuilder.fromUri(restConfig.endpoint()).path(HENTDOKUMENT)
            .resolveTemplate("journalpostId", q.journalpostId())
            .resolveTemplate("dokumentInfoId", q.dokumentId())
            .resolveTemplate("variantFormat", q.variantFormat())
            .build();
        var request = RestRequest.newGET(path, restConfig);
        var doc = restKlient.sendReturnByteArray(request);
        return doc;
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest req, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(req, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {

        var method = new RestRequest.Method(RestRequest.WebMethod.POST, HttpRequest.BodyPublishers.ofString(req.toHttpJsonBody()));
        var request = RestRequest.newRequest(method, graphql, restConfig);
        var res = restKlient.send(request, clazz);
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), restConfig.endpoint(), F_240613);
        }
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + restConfig.endpoint() + "]";
    }
}
