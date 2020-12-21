package no.nav.vedtak.felles.integrasjon.saf;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.felles.integrasjon.saf.SafJerseyTjeneste.SafTjenesteFeil.FEILFACTORY;

import java.net.URI;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.inject.Inject;
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
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOIDCClient;
import no.nav.vedtak.konfig.KonfigVerdi;

//@Dependent
public class SafJerseyTjeneste extends AbstractJerseyOIDCClient {

    private static final String DEFAULT_BASE = "https://localhost:8063/rest/api/saf";
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String GRAPHQL = "/graphql";
    private final URI base;

    @Inject
    public SafJerseyTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_BASE) URI base) {
        super(objectMapper());
        this.base = base;
    }

    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest q, DokumentoversiktResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), DokumentoversiktFagsakQueryResponse.class).dokumentoversiktFagsak();
    }

    public Journalpost hentJournalpostInfo(JournalpostQueryRequest q, JournalpostResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), JournalpostQueryResponse.class).journalpost();
    }

    public List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest q, JournalpostResponseProjection p) {
        return utførSpørring(new GraphQLRequest(q, p), TilknyttedeJournalposterQueryResponse.class).tilknyttedeJournalposter();
    }

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
            throw FEILFACTORY.safForespørselFeilet(q.toString(), e).toException();
        }
    }

    private <T extends GraphQLResult<?>> T utførSpørring(GraphQLRequest req, Class<T> clazz) {
        try {
            return (validate(utførForespørsel(req, clazz)));
        } catch (WebApplicationException e) {
            throw FEILFACTORY.safForespørselFeilet(req.toQueryString(), e).toException();
        }
    }

    private static <T extends GraphQLResult<?>> T validate(T res) {
        if (res.getErrors() != null && res.getErrors().size() > 0) {
            var feilmelding = res.getErrors().stream()
                    .map(GraphQLError::getMessage)
                    .collect(Collectors.joining("\n Error: "));
            throw FEILFACTORY.forespørselReturnerteFeil(feilmelding).toException();
        }
        return res;
    }

    private <T extends GraphQLResult<?>> T utførForespørsel(GraphQLRequest req, Class<T> clazz) {
        try {
            return client.target(base)
                    .path(GRAPHQL)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz);
        } catch (WebApplicationException e) {
            throw FEILFACTORY.safForespørselFeilet(req.toQueryString(), e).toException();
        }
    }

    private static ObjectMapper objectMapper() {
        return getObjectMapper()
                .copy()
                .setPropertyNamingStrategy(LOWER_CAMEL_CASE)
                .setTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
                .disable(WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(FAIL_ON_EMPTY_BEANS)
                .configure(WRITE_BIGDECIMAL_AS_PLAIN, true)
                .enable(FAIL_ON_READING_DUP_TREE_KEY)
                .enable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    interface SafTjenesteFeil extends DeklarerteFeil {
        SafTjenesteFeil FEILFACTORY = FeilFactory.create(SafTjenesteFeil.class);

        @TekniskFeil(feilkode = "F-240613", feilmelding = "Forespørsel til SAF feilet for spørring %s", logLevel = LogLevel.WARN)
        Feil safForespørselFeilet(String query, Throwable t);

        @TekniskFeil(feilkode = "F-588730", feilmelding = "Feil fra SAF ved utført query. Error: %s", logLevel = LogLevel.WARN)
        Feil forespørselReturnerteFeil(String response);
    }
}
