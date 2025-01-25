package no.nav.foreldrepenger.sikkerhet.populasjon;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonEksternRequest;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonInternRequest;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonKlient;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class TilgangKlient implements PopulasjonKlient {

    private static final Logger LOG = LoggerFactory.getLogger(TilgangKlient.class);

    private final URI internBrukerUri;

    private final URI eksternBrukerUri;
    private final RestClient klient;
    private final RestConfig restConfig;

    public TilgangKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.internBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/internbruker")
            .build();
        this.eksternBrukerUri = UriBuilder.fromUri(restConfig.fpContextPath())
            .path("/api/populasjon/eksternbruker")
            .build();
    }

    @Override
    public Tilgangsvurdering vurderTilgang(PopulasjonInternRequest request) {
        var rrequest = RestRequest.newPOSTJson(request, internBrukerUri, restConfig);
        return klient.send(rrequest, Tilgangsvurdering.class);
        //return Tilgangsvurdering.godkjenn();
    }

    @Override
    public Tilgangsvurdering vurderTilgang(PopulasjonEksternRequest request) {
        var rrequest = RestRequest.newPOSTJson(request, eksternBrukerUri, restConfig);
        return klient.send(rrequest, Tilgangsvurdering.class);
        //return Tilgangsvurdering.godkjenn();
    }
}
