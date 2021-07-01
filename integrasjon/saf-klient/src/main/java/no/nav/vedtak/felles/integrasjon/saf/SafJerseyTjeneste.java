package no.nav.vedtak.felles.integrasjon.saf;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
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
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey
public class SafJerseyTjeneste extends AbstractJerseyOidcRestClient implements Saf {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String F_240613 = "F-240613";
    private static final String DEFAULT_BASE = "https://saf.nais.adeo.no";
    private static final String GRAPHQL = "/graphql";

    private static final Logger LOG = LoggerFactory.getLogger(SafJerseyTjeneste.class);
    private final URI base;
    private final GraphQLErrorHandler errorHandler;

    

    @Inject
    public SafJerseyTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_BASE) URI base) {
        this(base, new SafErrorHandler());
    }

    SafJerseyTjeneste(URI base, GraphQLErrorHandler errorHandler) {
        this.base = base;
        this.errorHandler = errorHandler;
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
            var doc = invoke(client.target(base)
                    .path(HENTDOKUMENT)
                    .resolveTemplate("journalpostId", q.journalpostId())
                    .resolveTemplate("dokumentInfoId", q.dokumentId())
                    .resolveTemplate("variantFormat", q.variantFormat())
                    .request()
                    .buildGet(), byte[].class);
            LOG.info("Hentet dokument OK");
            return doc;
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest req, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(req, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
            LOG.trace("Eksekverer GraphQL query {}", req.getClass().getSimpleName());
            var res = invoke(client.target(base)
                    .path(GRAPHQL)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody())), clazz);
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
