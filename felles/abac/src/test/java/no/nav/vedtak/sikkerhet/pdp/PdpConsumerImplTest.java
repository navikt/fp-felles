package no.nav.vedtak.sikkerhet.pdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import no.nav.vedtak.exception.TekniskException;

public class PdpConsumerImplTest {
    private static HttpServer httpServer;
    private static List<Object[]> responses = new ArrayList<>();
    private static int port = 8000;
    private static String server = "http://localhost:" + port;
    private static String context = "/asm-pdp/authorize";
    private static final String fakeEndPoint = server + context;
    private final JsonObject jsonRequest = Json.createObjectBuilder().add("Request", "dummy").build();


    @SuppressWarnings("resource")
    @BeforeAll
    public static void setUp() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext(context, exchange -> {
            final Object[] resp = responses.remove(0);
            byte[] bytes = ((String) resp[1]).getBytes();
            exchange.sendResponseHeaders((Integer) resp[0], bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        httpServer.start();
    }



    @AfterAll
    public static void tearDown() throws Exception {
        httpServer.stop(0);
    }

    @Test
    public void en_401_lager_ny_klient_og_logger_det() {
        final String response1 = "{\"response\":1}";
        final String response3 = "{\"response\":3}";

        responses.clear();
        responses.add(new Object[] { HttpURLConnection.HTTP_OK, response1 });
        responses.add(new Object[] { HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized" });
        responses.add(new Object[] { HttpURLConnection.HTTP_OK, response3 });

        PdpConsumerImpl impl = new PdpConsumerImpl(fakeEndPoint, "user", "pass");

        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response1);
        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response3);
    }

    @Test
    public void to_401_på_rad_gir_exception() {
        final String response1 = "{\"response\":1}";

        responses.clear();
        responses.add(new Object[] { HttpURLConnection.HTTP_OK, response1 });
        responses.add(new Object[] { HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized" });
        responses.add(new Object[] { HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized" });

        PdpConsumerImpl impl = new PdpConsumerImpl(fakeEndPoint, "user", "pass");

        assertThat(impl.execute(jsonRequest).toString()).isEqualTo(response1);
        assertThatExceptionOfType(TekniskException.class).isThrownBy(() -> impl.execute(jsonRequest))
                .withMessageStartingWith("F-867412");
    }
}
