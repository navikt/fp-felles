package no.nav.vedtak.felles.integrasjon.spokelse;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;

@NativeKlient
@ApplicationScoped
public class SpøkelseNativeKlient implements Spøkelse {

    private static final Logger LOG = LoggerFactory.getLogger(SpøkelseNativeKlient.class);

    private RestKlient restKlient;
    private URI uri;
    private String scope;

    @Inject
    public SpøkelseNativeKlient(RestKlient restKlient,
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_URL", defaultVerdi = "http://spokelse.tbd/grunnlag") URI uri,
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_SCOPES", defaultVerdi = "api://prod-fss.tbd.spokelse/.default") String scope) {
        this.restKlient = restKlient;
        this.uri = uri;
        this.scope = scope;
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
            var request = restKlient.request().builderSystemAzure(scope)
                .uri(path)
                .GET()
                .build();
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
