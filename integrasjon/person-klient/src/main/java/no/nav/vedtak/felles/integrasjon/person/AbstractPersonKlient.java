package no.nav.vedtak.felles.integrasjon.person;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;

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
import no.nav.pdl.HentPersonBolkQueryRequest;
import no.nav.pdl.HentPersonBolkQueryResponse;
import no.nav.pdl.HentPersonBolkResult;
import no.nav.pdl.HentPersonBolkResultResponseProjection;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.HentPersonQueryResponse;
import no.nav.pdl.Identliste;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

public abstract class AbstractPersonKlient implements Persondata {

    private final PdlDefaultErrorHandler errorHandler;
    private final Tema tema;
    private final Ytelse defaultYtelse;
    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected AbstractPersonKlient() {
        this(Tema.FOR);
    }

    protected AbstractPersonKlient(Tema tema) {
        this(RestClient.client(), tema);
    }

    protected AbstractPersonKlient(RestClient restKlient, Tema tema) {
        this(restKlient, tema, Ytelse.FORELDREPENGER);
    }

    protected AbstractPersonKlient(RestClient restKlient, Tema tema, Ytelse ytelse) {
        this.restKlient = restKlient;
        this.restConfig = RestConfig.forClient(this.getClass());
        this.tema = tema;
        this.defaultYtelse = ytelse;
        this.errorHandler = new PdlDefaultErrorHandler();
    }

    @Override
    public GeografiskTilknytning hentGT(HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p) {
        return query(defaultYtelse, q, p, HentGeografiskTilknytningQueryResponse.class).hentGeografiskTilknytning();
    }

    @Override
    public GeografiskTilknytning hentGT(Ytelse ytelse, HentGeografiskTilknytningQueryRequest q, GeografiskTilknytningResponseProjection p) {
        return query(ytelse, q, p, HentGeografiskTilknytningQueryResponse.class).hentGeografiskTilknytning();
    }

    @Override
    public Person hentPerson(HentPersonQueryRequest q, PersonResponseProjection p) {
        return query(defaultYtelse, q, p, HentPersonQueryResponse.class).hentPerson();
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
    public List<HentPersonBolkResult> hentPersonBolk(HentPersonBolkQueryRequest q, HentPersonBolkResultResponseProjection p) {
        return query(defaultYtelse, q, p, HentPersonBolkQueryResponse.class).hentPersonBolk();
    }

    @Override
    public Person hentPerson(Ytelse ytelse, HentPersonQueryRequest q, PersonResponseProjection p) {
        return query(ytelse, q, p, HentPersonQueryResponse.class).hentPerson();
    }

    @Override
    public Person hentPerson(Ytelse ytelse, HentPersonQueryRequest q, PersonResponseProjection p, boolean ignoreNotFound) {
        try {
            return hentPerson(ytelse, q, p);
        } catch (PdlException e) {
            if (e.getStatus() == HttpURLConnection.HTTP_NOT_FOUND && ignoreNotFound) {
                return null;
            }
            throw e;
        }
    }

    @Override
    public List<HentPersonBolkResult> hentPersonBolk(Ytelse ytelse, HentPersonBolkQueryRequest q, HentPersonBolkResultResponseProjection p) {
        return query(ytelse, q, p, HentPersonBolkQueryResponse.class).hentPersonBolk();
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
        return query(null, new GraphQLRequest(q, p), clazz);
    }

    @Override
    public <T extends GraphQLResult<?>> T query(Ytelse ytelse, GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz) {
        return query(ytelse, new GraphQLRequest(q, p), clazz);
    }

    private <T extends GraphQLResult<?>> T query(Ytelse ytelse, GraphQLRequest req, Class<T> clazz) {
        var method = new RestRequest.Method(RestRequest.WebMethod.POST, HttpRequest.BodyPublishers.ofString(req.toHttpJsonBody()));
        var restRequest = RestRequest.newRequest(method, restConfig.endpoint(), restConfig).header("TEMA", tema.name());
        Optional.ofNullable(ytelse).ifPresent(bnr -> restRequest.header("behandlingsnummer", bnr.getBehandlingsnummer()));
        var res = restKlient.send(restRequest, clazz);
        if (res.hasErrors()) {
            return errorHandler.handleError(res.getErrors(), restConfig.endpoint(), PDL_ERROR_RESPONSE);
        }
        return res;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + restConfig.endpoint() + ", errorHandler=" + errorHandler + "]";
    }

}
