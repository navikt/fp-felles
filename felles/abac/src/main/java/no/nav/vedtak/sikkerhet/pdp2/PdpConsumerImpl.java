package no.nav.vedtak.sikkerhet.pdp2;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlRequestBuilder2;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlResponse;

@ApplicationScoped
public class PdpConsumerImpl implements Pdp2Consumer {

    private static final String DEFAULT_ABAC_URL = "http://abac-foreldrepenger.teamabac/application/authorize";
    private static final String PDP_ENDPOINT_URL_KEY = "abac.pdp.endpoint.url";
    private static final String SYSTEMBRUKER_USERNAME = "systembruker.username";
    private static final String SYSTEMBRUKER_PASSWORD = "systembruker.password"; // NOSONAR
    private static final String MEDIA_TYPE = "application/xacml+json";
    private static final Logger LOG = LoggerFactory.getLogger(PdpConsumerImpl.class);

    private HttpClient client;
    private ObjectReader reader;

    private URI pdpUrl;
    private String brukernavn;
    private String basicCredentials;

    PdpConsumerImpl() {
    } // CDI

    /*
     * TODO(jol) vurder å hente det som injectes med ENV.getProperty - da kan hele saken gjøres static ....
     */
    @Inject
    public PdpConsumerImpl(@KonfigVerdi(value = PDP_ENDPOINT_URL_KEY, defaultVerdi = DEFAULT_ABAC_URL) URI pdpUrl,
                           @KonfigVerdi(SYSTEMBRUKER_USERNAME) String brukernavn,
                           @KonfigVerdi(SYSTEMBRUKER_PASSWORD) String passord) {
        this.pdpUrl = pdpUrl;
        this.brukernavn = brukernavn;
        this.basicCredentials = basicCredentials(brukernavn, passord);
        // TODO - vurder om bør settes static final?
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.reader = DefaultJsonMapper.getObjectMapper().readerFor(XacmlResponse.class);
    }

    @Override
    public XacmlResponse evaluate(XacmlRequestBuilder2 xacmlRequestBuilder) {
        var xacmlRequest = xacmlRequestBuilder.build();
        LOG.trace("PDP2 svar {}", xacmlRequest);
        // TODO : hvilke headere trenger abac egentlig - utenom Auth og Content-type
        var request = HttpRequest.newBuilder()
            .header("Authorization", basicCredentials)
            .header("Nav-Consumer-Id", brukernavn)
            .header("Nav-Call-Id", MDCOperations.getCallId())
            .header("Nav-Callid", MDCOperations.getCallId())
            .header("Content-type", MEDIA_TYPE)
            .timeout(Duration.ofSeconds(5))
            .uri(pdpUrl)
            .POST(HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(xacmlRequest), UTF_8))
            .build();

        try {
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.statusCode() == 401 || response.body() == null) {
                LOG.info("ingen response fra PDP status = {}", response == null ? "null" : response.statusCode());
                throw new TekniskException("F-157385", "Kunne ikke hente svar fra ABAC");
            }
            var resultat = reader.readValue(response.body(), XacmlResponse.class);
            LOG.trace("PDP2 svar {}", resultat);
            return resultat;
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new TekniskException("F-091324", "Uventet IO-exception mot PDP", e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-432938", "InterruptedException ved henting av token", e);
        }
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }

}
