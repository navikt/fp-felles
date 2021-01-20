package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

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
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Jersey
public class JerseyPdlKlient extends AbstractJerseyPdlKlient implements Pdl {

    public JerseyPdlKlient(StsAccessTokenConfig config) {
        this(config, FOR);
    }

    public JerseyPdlKlient(StsAccessTokenConfig config, String tema) {
        super(config, tema);
    }

    @Inject
    public JerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            StsAccessTokenConfig config,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        super(config, endpoint, tema);
    }

    JerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        super(endpoint, filters);
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
    public Identliste hentIdenter(HentIdenterQueryRequest q, IdentlisteResponseProjection p) {
        return query(q, p, HentIdenterQueryResponse.class).hentIdenter();
    }

    @Override
    public List<HentIdenterBolkResult> hentIdenterBolkResults(HentIdenterBolkQueryRequest q, HentIdenterBolkResultResponseProjection p) {
        return query(q, p, HentIdenterBolkQueryResponse.class).hentIdenterBolk();
    }
}
