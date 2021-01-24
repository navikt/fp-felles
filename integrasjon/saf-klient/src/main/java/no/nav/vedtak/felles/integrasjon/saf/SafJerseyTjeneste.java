package no.nav.vedtak.felles.integrasjon.saf;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

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
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLDefaultErrorHandler;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Jersey
public class SafJerseyTjeneste extends AbstractJerseyOidcRestClient implements Saf {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String F_240613 = "F-240613";
    private static final String DEFAULT_BASE = "https://localhost:8063/rest/api/saf";
    private static final String GRAPHQL = "/graphql";

    private URI base;
    private GraphQLErrorHandler errorHandler;

    SafJerseyTjeneste() {

    }

    @Inject
    public SafJerseyTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_BASE) URI base) {
        this(base, new GraphQLDefaultErrorHandler());
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
        try {
            return client.target(base)
                    .path(HENTDOKUMENT)
                    .resolveTemplate("journalpostId", q.getJournalpostId())
                    .resolveTemplate("dokumentInfoId", q.getDokumentInfoId())
                    .resolveTemplate("variantFormat", q.getVariantFormat())
                    .request()
                    .get(byte[].class);
        } catch (WebApplicationException e) {
            throw new TekniskException(F_240613, base, e);
        }
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest req, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(req, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        try {
            var res = client.target(base)
                    .path(GRAPHQL)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz);
            if (res.hasErrors()) {
                return errorHandler.handleError(res.getErrors(), base, F_240613);
            }
            return res;
        } catch (WebApplicationException e) {
            throw new TekniskException(F_240613, base, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + base + "]";
    }
}
