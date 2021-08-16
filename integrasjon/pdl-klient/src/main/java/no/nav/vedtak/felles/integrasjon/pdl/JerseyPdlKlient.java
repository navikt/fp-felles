package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;

@Dependent
@Jersey
@Deprecated(since = "4.0.x", forRemoval = true)
/**
 * 
 * Denne klienten prøver først å finne et brukertoken på tråden. Om dette ikke
 * finnes benyttes et system token. Dette kan være litt forvirrende, og det
 * anbefales at man bruker en av de mer spesialierte implementasjonene som
 * bruker kun ett av disse
 *
 * @See OnBehalfOfJerseyPdlKlient
 * @See SystemJerseyPdlKlient
 */
public class JerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public JerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            StsAccessTokenConfig config,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        this(endpoint, new StsAccessTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
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
        super(endpoint, filters);
    }
}
