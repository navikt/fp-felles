package no.nav.vedtak.felles.integrasjon.pdl;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;

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
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.graphql.GraphQLErrorHandler;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Jersey
public class JerseyPdlKlient extends AbstractJerseyRestClient implements Pdl {

    private static final Logger LOG = LoggerFactory.getLogger(JerseyPdlKlient.class);
    private static final String HTTP_PDL_API_DEFAULT_GRAPHQL = "http://pdl-api.default/graphql";
    private static final String FOR = "FOR";

    private final URI endpoint;
    private final GraphQLErrorHandler errorHandler;

    @Inject
    public JerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            StsAccessTokenConfig config,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        this(endpoint, new PdlDefaultErrorHandler(), new StsAccessTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
    }

    public JerseyPdlKlient(StsAccessTokenConfig config) {
        this(config, FOR);
    }

    public JerseyPdlKlient(StsAccessTokenConfig config, String tema) {
        this(config, HTTP_PDL_API_DEFAULT_GRAPHQL, tema);
    }

    public JerseyPdlKlient(StsAccessTokenConfig config, String endpoint, String tema) {
        this(URI.create(endpoint), config, tema);
    }

    JerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        this(endpoint, new PdlDefaultErrorHandler(), filters);
    }

    JerseyPdlKlient(URI endpoint, GraphQLErrorHandler errorHandler, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
        this.errorHandler = errorHandler;
        LOG.info("Klient konstruert");
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
        try {
            LOG.info("Henter resultat for {} fra {}", clazz.getName(), endpoint);
            var res = client.target(endpoint)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req.toHttpJsonBody()))
                    .invoke(clazz);
            if (res.hasErrors()) {
                return errorHandler.handleError(res.getErrors(), endpoint, PDL_ERROR_RESPONSE);
            }
            LOG.info("Hentet resultat for {} fra {} OK", clazz.getName(), endpoint);
            return res;
        } catch (ProcessingException e) {
            if (e.getCause() != null && e.getCause() instanceof VLException) {
                throw VLException.class.cast(e.getCause());
            }
            throw e;
        }
    }

}
