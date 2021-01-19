package no.nav.vedtak.felles.integrasjon.saf;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;
import static java.util.TimeZone.getTimeZone;
import static java.util.stream.Collectors.joining;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
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
import no.nav.saf.TilknyttedeJournalposterQueryRequest;
import no.nav.saf.TilknyttedeJournalposterQueryResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Named("jersey")
public class SafJerseyTjeneste extends AbstractJerseyOidcRestClient implements Saf {

    private static final String F_240613 = "F-240613";
    private static final String DEFAULT_BASE = "https://localhost:8063/rest/api/saf";
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String GRAPHQL = "/graphql";
    private final URI base;

    @Inject
    public SafJerseyTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_BASE) URI base) {
        super(objectMapper());
        this.base = base;
    }

    @Override
    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest q, DokumentoversiktResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), DokumentoversiktFagsakQueryResponse.class).dokumentoversiktFagsak();
    }

    @Override
    public Journalpost hentJournalpostInfo(JournalpostQueryRequest q, JournalpostResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), JournalpostQueryResponse.class).journalpost();
    }

    @Override
    public List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest q, JournalpostResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), TilknyttedeJournalposterQueryResponse.class).tilknyttedeJournalposter();
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

    private <T extends GraphQLResult<?>> T utførSpørring(GraphQLRequest req, Class<T> clazz) {
        try {
            return validate(client.target(base)
                    .path(GRAPHQL)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz));
        } catch (WebApplicationException e) {
            throw new TekniskException(F_240613, base, e);
        }
    }

    private <T extends GraphQLResult<?>> T validate(T res) {
        if (res.getErrors() != null && res.getErrors().size() > 0) {
            var feil = res.getErrors().stream()
                    .map(GraphQLError::getMessage)
                    .collect(joining("\n Error: "));
            throw new TekniskException("F-588730", feil);
        }
        return res;
    }

    private static ObjectMapper objectMapper() {
        return mapper
                .copy()
                .setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .setTimeZone(getTimeZone("Europe/Oslo"))
                .disable(WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(FAIL_ON_EMPTY_BEANS)
                .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
                .enable(FAIL_ON_READING_DUP_TREE_KEY)
                .enable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + base + "]";
    }
}
