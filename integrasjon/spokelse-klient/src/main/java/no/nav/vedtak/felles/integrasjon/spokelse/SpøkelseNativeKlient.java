package no.nav.vedtak.felles.integrasjon.spokelse;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC,
    endpointProperty = "SPOKELSE_GRUNNLAG_URL", endpointDefault = "http://spokelse.tbd/grunnlag",
    scopesProperty = "SPOKELSE_GRUNNLAG_SCOPES", scopesDefault = "api://prod-fss.tbd.spokelse/.default")
@ApplicationScoped
public class SpøkelseNativeKlient implements Spøkelse {

    private static final Logger LOG = LoggerFactory.getLogger(SpøkelseNativeKlient.class);

    private RestClient restKlient;
    private URI uri;

    @Inject
    public SpøkelseNativeKlient(RestClient restKlient) {
        this.restKlient = restKlient;
        this.uri = RestConfig.endpointFromAnnotation(SpøkelseNativeKlient.class);
    }

    SpøkelseNativeKlient() {
        // CDI
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlag(String fnr) {
        try {
            var path = UriBuilder.fromUri(uri)
                .queryParam("fodselsnummer", fnr)
                .build();
            var request = RestRequest.newRequest(RestRequest.Method.get(), path, SpøkelseNativeKlient.class);
            var grunnlag = restKlient.send(request, SykepengeVedtak[].class);
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            throw new TekniskException( "FP-180126", String.format("SPokelse %s gir feil, ta opp med team sykepenger.", uri.toString()), e);
        }
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr) {
        try {
            return hentGrunnlag(fnr);
        } catch (Exception e) {
            LOG.info("SPokelse felles: feil ved oppslag mot {}, returnerer ingen grunnlag", uri, e);
            return List.of();
        }
    }
}
