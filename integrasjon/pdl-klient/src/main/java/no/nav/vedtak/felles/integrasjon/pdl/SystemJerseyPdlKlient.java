package no.nav.vedtak.felles.integrasjon.pdl;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.SystemTokenClientRequestFilter;

@Dependent
@Jersey("system")
public class SystemJerseyPdlKlient extends AbstractJerseyPdlKlient {

    @Inject
    public SystemJerseyPdlKlient(
            @KonfigVerdi(value = "pdl.base.url", defaultVerdi = HTTP_PDL_API_DEFAULT_GRAPHQL) URI endpoint,
            StsAccessTokenConfig config,
            @KonfigVerdi(value = "pdl.tema", defaultVerdi = FOR) String tema) {
        super(endpoint, new SystemTokenClientRequestFilter(new StsAccessTokenJerseyClient(config), tema));
    }
}
