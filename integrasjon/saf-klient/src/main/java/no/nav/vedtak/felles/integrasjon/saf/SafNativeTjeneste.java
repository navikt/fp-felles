package no.nav.vedtak.felles.integrasjon.saf;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "saf.base.url", endpointDefault = "https://saf.nais.adeo.no",
    scopesProperty = "saf.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.saf/.default")
@ApplicationScoped
public class SafNativeTjeneste extends AbstractJerseyOidcRestClient implements Saf {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String F_240613 = "F-240613";
    private static final String GRAPHQL = "/graphql";

    private static final Logger LOG = LoggerFactory.getLogger(SafNativeTjeneste.class);

    private RestClient restKlient;
    private URI base;
    private URI graphql;
    private String scopes;
    private GraphQLErrorHandler errorHandler;

    @Inject
    public SafNativeTjeneste(RestClient restKlient) {
        this.restKlient = restKlient;
        this.base = RestConfig.endpointFromAnnotation(SafNativeTjeneste.class);
        this.scopes = RestConfig.scopesFromAnnotation(SafNativeTjeneste.class);
        this.graphql = URI.create(this.base.toString() + GRAPHQL);
        this.errorHandler = new SafErrorHandler();
    }

    SafNativeTjeneste() {
        // CDI proxyable
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
        LOG.trace("Henter dokument");
        var path = UriBuilder.fromUri(base).path(HENTDOKUMENT)
            .resolveTemplate("journalpostId", q.journalpostId())
            .resolveTemplate("dokumentInfoId", q.dokumentId())
            .resolveTemplate("variantFormat", q.variantFormat())
            .build();
        var request = RestRequest.newRequest(RestRequest.Method.get(), path, TokenFlow.ADAPTIVE, scopes);
        var doc = restKlient.sendReturnByteArray(request);
        LOG.info("Hentet dokument OK");
        return doc;
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest req, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(req, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        LOG.trace("Eksekverer GraphQL query {}", req.getClass().getSimpleName());
        var method = new RestRequest.Method(RestRequest.WebMethod.POST, HttpRequest.BodyPublishers.ofString(req.toHttpJsonBody()));
        var request = RestRequest.newRequest(method, graphql, TokenFlow.ADAPTIVE, scopes);
        var res = restKlient.send(request, clazz);
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), base, F_240613);
        }
        LOG.info("Eksekvert GraphQL query OK");
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + base + "]";
    }
}
