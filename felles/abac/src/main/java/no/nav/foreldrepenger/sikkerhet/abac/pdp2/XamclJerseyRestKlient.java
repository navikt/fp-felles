package no.nav.foreldrepenger.sikkerhet.abac.pdp2;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponse;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
class XamclJerseyRestKlient extends AbstractJerseyRestClient implements NyXacmlConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(XamclJerseyRestKlient.class);
    private static final String DEFAULT_ABAC_URL = "http://abac-foreldrepenger.teamabac/application/authorize";
    private static final String PDP_ENDPOINT_URL_KEY = "abac.pdp.endpoint.url";
    private static final String SYSTEMBRUKER_USERNAME = "systembruker.username";
    private static final String SYSTEMBRUKER_PASSWORD = "systembruker.password"; // NOSONAR
    private static final String MEDIA_TYPE = "application/xacml+json";

    private final URI endpoint;
    private final HttpAuthenticationFeature basicAuthFeature;

    @Inject
    public XamclJerseyRestKlient(
        @KonfigVerdi(value = PDP_ENDPOINT_URL_KEY, defaultVerdi = DEFAULT_ABAC_URL) URI endpoint,
        @KonfigVerdi(SYSTEMBRUKER_USERNAME) String brukernavn,
        @KonfigVerdi(SYSTEMBRUKER_PASSWORD) String passord) {
        super();
        this.endpoint = endpoint;
        basicAuthFeature = HttpAuthenticationFeature.basic(brukernavn, passord);
    }

    @Override
    public XacmlResponse evaluate(final XacmlRequest request) {
        try {
            var target = client
                .register(basicAuthFeature)
                .target(endpoint);
            LOG.info("Sjekker ABAC på: {}", target.getUri());

            var res = target
                .request(MEDIA_TYPE)
                .post(Entity.entity(request, MEDIA_TYPE));

            if (res.getStatusInfo().getStatusCode() != 200) {
                LOG.info("Feil fra ABAC, HTTP {} - {}, svaret ble: {}", res.getStatus(), res.getStatusInfo().getReasonPhrase(), res.readEntity(String.class));
            } else {
                LOG.info("ABAC svarte OK.");
                return res.readEntity(XacmlResponse.class);
            }
        } catch (Exception e) {
            LOG.info("Exception: Kunne ikke evaluere ABAC.", e);
            throw e;
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [uri=" + endpoint + "]";
    }
}
