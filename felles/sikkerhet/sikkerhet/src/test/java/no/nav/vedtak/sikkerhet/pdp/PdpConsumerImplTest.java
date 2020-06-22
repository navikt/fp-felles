package no.nav.vedtak.sikkerhet.pdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.glassfish.json.JsonUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import net.shibboleth.utilities.java.support.collection.Pair;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;

public class PdpConsumerImplTest {
    private static HttpServer httpServer;
    private static List<Pair<Integer, String>> responses = new ArrayList<>();
    private static int port = 8000;
    private static String server = "http://localhost:" + port;
    private static String context = "/asm-pdp/authorize";
    private static final String fakeEndPoint = server + context;
    private final JsonObject jsonRequest = JsonUtil.toJson("{\"Request\": \"dummy\"}").asJsonObject();
    @Rule
    public LogSniffer sniffer = new LogSniffer();

    @SuppressWarnings("resource")
    @BeforeClass
    public static void setUp() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext(context, exchange -> {
            final Pair<Integer, String> resp = responses.remove(0);
            byte[] bytes = resp.getSecond().getBytes();
            exchange.sendResponseHeaders(resp.getFirst(), bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        httpServer.start();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        httpServer.stop(0);
    }

    @Test
    public void en_401_lager_ny_klient_og_logger_det() {
        final String response1 = "{\"response\":1}";
        final String response3 = "{\"response\":3}";

        responses.clear();
        responses.add(new Pair<>(HttpURLConnection.HTTP_OK, response1));
        responses.add(new Pair<>(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"));
        responses.add(new Pair<>(HttpURLConnection.HTTP_OK, response3));

        PdpConsumerImpl impl = new PdpConsumerImpl(fakeEndPoint, "user", "pass");

        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response1);
        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response3);
        sniffer.assertHasWarnMessage("F-563467");
    }

    @Test
    public void to_401_på_rad_gir_exception() {
        final String response1 = "{\"response\":1}";

        responses.clear();
        responses.add(new Pair<>(HttpURLConnection.HTTP_OK, response1));
        responses.add(new Pair<>(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"));
        responses.add(new Pair<>(HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"));

        PdpConsumerImpl impl = new PdpConsumerImpl(fakeEndPoint, "user", "pass");

        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response1);
        assertThatExceptionOfType(TekniskException.class).isThrownBy(() -> impl.execute(jsonRequest))
            .withMessageStartingWith("F-867412");
        sniffer.assertHasWarnMessage("F-563467");
    }

    @Test
    @Ignore("Kjører ikke integrasjonstester i vanlige bygg")
    public void gjør_kall_på_PDP() {
        /*
           Bruk f.eks srvfpfordel som $service-account
           Credential srvfpfordel (q | fss | - | fpfordel)
            -> https://fasit.adeo.no/resources/4093802
           Create token
           -> curl --insecure --silent --user <insert service-account>:<insert service-account password> "https://security-token-service.nais.preprod.local/rest/v1/sts/token?grant_type=client_credentials&scope=openid" --header "accept: application/json"; echo
         */
        String token = "<insert token>";
        String serviceAccount = "<insert service-account>";
        String serviceAccountPassword = "<insert service-account password>";
        String requestJson = String.format("{\"Request\":{\"Resource\":{\"Attribute\":[{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.domene\",\"Value\":\"foreldrepenger\"},{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.resource_type\",\"Value\":\"no.nav.abac.attributter.foreldrepenger\"}]},\"Action\":{\"Attribute\":[{\"AttributeId\":\"urn:oasis:names:tc:xacml:1.0:action:action-id\",\"Value\":\"read\"}]},\"Environment\":{\"Attribute\":[{\"AttributeId\":\"no.nav.abac.attributter.environment.felles.pep_id\",\"Value\":\"srvengangsstonad\"},{\"AttributeId\":\"no.nav.abac.attributter.environment.felles.oidc_token_body\",\"Value\":\"%s==\"}]}}}", token);

        PdpConsumerImpl impl = new PdpConsumerImpl("https://wasapp-q1.adeo.no/asm-pdp/authorize", serviceAccount, serviceAccountPassword);
        final JsonObject jsonResponse = impl.execute(JsonUtil.toJson(requestJson).asJsonObject());

        assertThat(jsonResponse.toString()).startsWith("{\"Response\":{\"Decision\":");
    }
}
