package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
/**
 *
 * @deprecated Bruk {@link ArbeidsfordelingJerseyRestKlient} som implementer
 *             samme grensesnittet på en bedre måte
 *
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public class ArbeidsfordelingRestKlient implements Arbeidsfordeling {

    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private OidcRestClient restClient;
    private URI alleEnheterUri;
    private URI besteEnhetUri;
    private String uriString;

    @Inject
    public ArbeidsfordelingRestKlient(OidcRestClient restClient,
            @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restClient = restClient;
        this.alleEnheterUri = uri;
        this.besteEnhetUri = URI.create(uri + BEST_MATCH);
        this.uriString = uri.toString();
    }

    ArbeidsfordelingRestKlient() {
    }

    @Override
    public List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, alleEnheterUri);
    }

    @Override
    public List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, besteEnhetUri);
    }

    private List<ArbeidsfordelingResponse> hentEnheterFor(ArbeidsfordelingRequest request, URI uri) {
        try {
            var respons = restClient.post(uri, request, ArbeidsfordelingResponse[].class);
            return Arrays.stream(respons)
                    .filter(response -> "AKTIV".equalsIgnoreCase(response.getStatus()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw ArbeidsfordelingRestKlientFeil.FACTORY.feilfratjeneste(uriString, e.getMessage(), e).toException();
        }
    }

    interface ArbeidsfordelingRestKlientFeil extends DeklarerteFeil {
        ArbeidsfordelingRestKlientFeil FACTORY = FeilFactory.create(ArbeidsfordelingRestKlientFeil.class);

        @IntegrasjonFeil(feilkode = "F-016913", feilmelding = "NORG2 arbeidsfordeling feil ved oppslag mot %s", logLevel = LogLevel.WARN)
        Feil feilfratjeneste(String var1, String msg, Throwable t);
    }

}
