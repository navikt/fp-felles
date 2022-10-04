package no.nav.vedtak.felles.integrasjon.person;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

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
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

public abstract class AbstractPersonKlient implements Persondata {

    private static Set<TokenFlow> REQUIRED_TOKEN = Set.of(TokenFlow.ADAPTIVE_ADD_CONSUMER, TokenFlow.CONTEXT_ADD_CONSUMER);
    private static final Logger LOG = LoggerFactory.getLogger(AbstractPersonKlient.class);

    private final PdlDefaultErrorHandler errorHandler;
    private final Tema tema;
    private final RestClient restKlient;
    private final RestConfig restConfig;


    protected AbstractPersonKlient() {
        this(Tema.FOR);
    }

    protected AbstractPersonKlient(Tema tema) {
        this(RestClient.client(), tema);
    }

    protected AbstractPersonKlient(RestClient restKlient, Tema tema) {
        this.restKlient = restKlient;
        this.restConfig = RestConfig.forClient(this.getClass());
        if (!REQUIRED_TOKEN.contains(this.restConfig.tokenConfig())) {
            throw new IllegalArgumentException("Utviklerfeil: Personklienter må ha en av _add_consumer");
        }
        this.tema = tema;
        this.errorHandler = new PdlDefaultErrorHandler();
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
            if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND && ignoreNotFound) {
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
        LOG.trace("Henter resultat for {} fra {}", clazz.getName(), restConfig.endpoint());
        var method = new RestRequest.Method(RestRequest.WebMethod.POST, HttpRequest.BodyPublishers.ofString(req.toHttpJsonBody()));
        var restRequest = RestRequest.newRequest(method, restConfig.endpoint(), restConfig)
            .header("TEMA", tema.name());
        var res = restKlient.send(restRequest, clazz);
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), restConfig.endpoint(), PDL_ERROR_RESPONSE);
        }
        LOG.trace("Hentet resultat for {} fra {} OK", clazz.getName(), restConfig.endpoint());
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + restConfig.endpoint() + ", errorHandler=" + errorHandler + "]";
    }

}
