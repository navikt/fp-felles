package no.nav.vedtak.felles.integrasjon.pdl;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentGeografiskTilknytningQueryResponse;
import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkQueryResponse;
import no.nav.pdl.HentIdenterBolkResult;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentIdenterQueryResponse;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.HentPersonQueryResponse;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

@NativeKlient
@ApplicationScoped
public class NativePdlKlient implements Pdl {

    private static final Logger LOG = LoggerFactory.getLogger(NativePdlKlient.class);
    static final String HTTP_PDL_API_DEFAULT_GRAPHQL = "http://pdl-api.pdl/graphql";
    public static final String FOR = "FOR";

    private URI endpoint;
    private GraphQLErrorHandler errorHandler;
    private String tema;
    private RestKlient restKlient;

    /**
     * TODO: Utvide med ulike varianter ifm azure - Bruker, System, OBO, etc. Evt deleger til TokenProvider/OidcRequest
     * Inntil videre brukes token fra kontekst (bruker eller system) + consumertoken
     */
    @Inject
    public NativePdlKlient(RestKlient restKlient,
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        this.restKlient = restKlient;
        this.endpoint = endpoint;
        this.tema = tema;
        this.errorHandler = new PdlDefaultErrorHandler();
    }

    NativePdlKlient() {
        // CDI proxyable
    }

    @Override
    public GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p) {
        return query(q, p, HentGeografiskTilknytningQueryResponse.class).hentGeografiskTilknytning();
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p) {
        return query(q, p, HentPersonQueryResponse.class).hentPerson();
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p, boolean ignoreNotFound) {
        try {
            return hentPerson(q, p);
        } catch (PdlException e) {
            if (e.getStatus() == SC_NOT_FOUND && ignoreNotFound) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p) {
        return query(q, p, HentIdenterQueryResponse.class).hentIdenter();
    }

    @Override
    public List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p) {
        return query(q, p, HentIdenterBolkQueryResponse.class).hentIdenterBolk();
    }

    @Override
    public <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
        return query(new GraphQLRequest(q, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(GraphQLRequest req, Class<T> clazz) {
        LOG.trace("Henter resultat for {} fra {}", clazz.getName(), endpoint);
        var request = RestRequest.builderConsumerToken(SikkerhetContext.BRUKER)
            .header("TEMA", tema)
            .uri(endpoint)
            .POST(HttpRequest.BodyPublishers.ofString(req.toHttpJsonBody()))
            .build();
        var res = restKlient.send(request, clazz);
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), endpoint, PDL_ERROR_RESPONSE);
        }
        LOG.trace("Hentet resultat for {} fra {} OK", clazz.getName(), endpoint);
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", errorHandler=" + errorHandler + "]";
    }

}
