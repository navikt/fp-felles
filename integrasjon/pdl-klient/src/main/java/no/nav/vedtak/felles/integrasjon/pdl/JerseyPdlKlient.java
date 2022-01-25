package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenProvider;

@Dependent
@Jersey
public class JerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public JerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema,
            StsAccessTokenProvider provider) {
        this(endpoint, new StsAccessTokenClientRequestFilter(provider, tema));
    }

    public JerseyPdlKlient() {
        this(FOR);
    }

    public JerseyPdlKlient(String tema) {
        this(HTTP_PDL_API_DEFAULT_GRAPHQL, tema);
    }

    public JerseyPdlKlient(String endpoint, String tema) {
        this(URI.create(endpoint), tema, null);
    }

    JerseyPdlKlient(URI endpoint, ClientRequestFilter... filters) {
        super(endpoint, filters);
    }
}
