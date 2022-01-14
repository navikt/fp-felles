package no.nav.vedtak.felles.integrasjon.spokelse;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.AzureADRestClient;

@ApplicationScoped
public class SpøkelseKlient implements Spøkelse {

    private static final Logger LOG = LoggerFactory.getLogger(SpøkelseKlient.class);

    private URI uri;
    private String uriString;
    private AzureADRestClient restClient;

    @Inject
    public SpøkelseKlient(
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_URL", defaultVerdi = "http://spokelse.tbd/grunnlag") URI uri,
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_SCOPES", defaultVerdi = "api://prod-fss.tbd.spokelse/.default") String scope) {
        this.restClient = AzureADRestClient.builder().scope(scope).build();
        this.uri = uri;
        this.uriString = uri.toString();
    }

    SpøkelseKlient() {
        // CDI
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlag(String fnr) {
        try {
            var request = new URIBuilder(uri)
                .addParameter("fodselsnummer", fnr)
                .build();
            var grunnlag = restClient.get(request, SykepengeVedtak[].class);
            var resultat = Arrays.asList(grunnlag);
            LOG.info("SPokelse felles {} fikk grunnlag {}", uriString, resultat);
            return resultat;
        } catch (Exception e) {
            LOG.info("SPokelse felles: feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            throw new TekniskException( "FP-180126", String.format("SPokelse %s gir feil, ta opp med team sykepenger.", uriString), e);
        }
    }

    @Override
    public List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr) {
        try {
            var request = new URIBuilder(uri)
                .addParameter("fodselsnummer", fnr)
                .build();
            var grunnlag = restClient.get(request, SykepengeVedtak[].class);
            var resultat = Arrays.asList(grunnlag);
            LOG.info("SPokelse felles {} fikk grunnlag {}", uriString, resultat);
            return resultat;
        } catch (Exception e) {
            LOG.info("SPokelse felles: feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            return List.of();
        }
    }
}
