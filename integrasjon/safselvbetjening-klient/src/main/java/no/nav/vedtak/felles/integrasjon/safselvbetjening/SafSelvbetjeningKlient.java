package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.safselvbetjening.Dokumentoversikt;
import no.nav.safselvbetjening.DokumentoversiktResponseProjection;
import no.nav.safselvbetjening.DokumentoversiktSelvbetjeningQueryRequest;
import no.nav.safselvbetjening.DokumentoversiktSelvbetjeningQueryResponse;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

/**
 * Client for retrieving documents from SAF Selvbetjening in the context of a logged-in citizen.
 * Documentation:
 * - <a href="https://github.com/navikt/safselvbetjening">GitHub: safselvbetjening</a>
 * - <a href="https://confluence.adeo.no/display/BOA/safselvbetjening">Confluence: safselvbetjening</a>
 */

@ApplicationScoped
@RestClientConfig(
        tokenConfig = TokenFlow.ADAPTIVE,
        endpointProperty = "safselvbetjening.base.url",
        endpointDefault = "https://safselvbetjening.prod-fss-pub.nais.io",
        scopesProperty = "safselvbetjening.scopes",
        scopesDefault = "api://prod-fss.teamdokumenthandtering.safselvbetjening/.default"
)
public class SafSelvbetjeningKlient implements SafSelvbetjening {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/ARKIV";
    private static final String F_240614 = "F-240614";
    private static final String GRAPHQL_PATH = "/graphql";

    private final RestClient restKlient;
    private final RestConfig restConfig;
    private final URI graphql;
    private final SafErrorHandler errorHandler;

    protected SafSelvbetjeningKlient() {
        this(RestClient.client());
    }

    protected SafSelvbetjeningKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.graphql = URI.create(this.restConfig.endpoint() + GRAPHQL_PATH);
        this.errorHandler = new SafErrorHandler();
    }

    @Override
    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktSelvbetjeningQueryRequest q, DokumentoversiktResponseProjection p) {
        return query(q, p, DokumentoversiktSelvbetjeningQueryResponse.class).dokumentoversiktSelvbetjening();
    }

    @Override
    public byte[] hentDokument(HentDokumentQuery q) {
        var path = UriBuilder.fromUri(restConfig.endpoint())
            .path(HENTDOKUMENT)
            .resolveTemplate("journalpostId", q.journalpostId())
            .resolveTemplate("dokumentInfoId", q.dokumentId())
            .build();
        var request = RestRequest.newGET(path, restConfig);
        return restKlient.sendReturnByteArray(request);
    }

    @Override
    public HttpResponse<byte[]> hentDokumentResponse(HentDokumentQuery q) {
        var path = UriBuilder.fromUri(restConfig.endpoint())
            .path(HENTDOKUMENT)
            .resolveTemplate("journalpostId", q.journalpostId())
            .resolveTemplate("dokumentInfoId", q.dokumentId())
            .build();
        var request = RestRequest.newGET(path, restConfig);
        return restKlient.sendReturnResponseByteArray(request);
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
            return errorHandler.handleError(res.getErrors(), restConfig.endpoint(), F_240614);
        }
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + restConfig.endpoint() + "]";
    }
}
