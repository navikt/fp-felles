package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.felles.integrasjon.rest.jersey.OidcRestJerseyClient;
import no.nav.vedtak.konfig.KonfigVerdi;

public class ArbeidsfordelingJerseyRestKlient extends OidcRestJerseyClient {

    private static final String DEFAULT_URI = "https://app.adeo.no/norg2/api/v1/arbeidsfordeling/enheter";
    private static final String BEST_MATCH = "/bestmatch";

    private final URI alleEnheterUri;
    private final URI besteEnhetUri;

    public ArbeidsfordelingJerseyRestKlient(
            @KonfigVerdi(value = "arbeidsfordeling.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.alleEnheterUri = uri;
        this.besteEnhetUri = URI.create(uri + BEST_MATCH);
    }

    public List<ArbeidsfordelingResponse> hentAlleAktiveEnheter(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, alleEnheterUri);
    }

    public List<ArbeidsfordelingResponse> finnEnhet(ArbeidsfordelingRequest request) {
        return hentEnheterFor(request, besteEnhetUri);
    }

    private List<ArbeidsfordelingResponse> hentEnheterFor(ArbeidsfordelingRequest request, URI uri) {
        try {
            return Arrays.stream(post(uri, request, ArbeidsfordelingResponse[].class))
                    .filter(response -> "AKTIV".equalsIgnoreCase(response.getStatus()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw ArbeidsfordelingRestKlientFeil.FACTORY.feilfratjeneste(alleEnheterUri.toString(), e.getMessage(), e).toException();
        }
    }

    interface ArbeidsfordelingRestKlientFeil extends DeklarerteFeil {
        ArbeidsfordelingRestKlientFeil FACTORY = FeilFactory.create(ArbeidsfordelingRestKlientFeil.class);

        @IntegrasjonFeil(feilkode = "F-016913", feilmelding = "NORG2 arbeidsfordeling feil ved oppslag mot %s", logLevel = LogLevel.WARN)
        Feil feilfratjeneste(String var1, String msg, Throwable t);
    }

}
