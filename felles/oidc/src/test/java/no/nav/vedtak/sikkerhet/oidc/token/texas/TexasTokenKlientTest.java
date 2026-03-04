package no.nav.vedtak.sikkerhet.oidc.token.texas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import no.nav.vedtak.exception.TekniskException;

class TexasTokenKlientTest {

    private static HttpServer server;
    private static TexasTokenKlient klient;
    private static String lastRequestBody;
    private static String lastContentType;
    private static String responseBody;
    private static int responseStatus;

    @BeforeAll
    static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        var port = server.getAddress().getPort();

        server.createContext("/token", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes());
            lastContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            sendResponse(exchange, responseStatus, responseBody);
        });

        server.createContext("/exchange", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes());
            lastContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            sendResponse(exchange, responseStatus, responseBody);
        });

        server.createContext("/introspect", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes());
            lastContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            sendResponse(exchange, responseStatus, responseBody);
        });

        server.start();

        var baseUri = "http://localhost:" + port;
        klient = new TexasTokenKlient(
            URI.create(baseUri + "/token"),
            URI.create(baseUri + "/introspect"),
            URI.create(baseUri + "/exchange")
        );
    }

    @AfterAll
    static void tearDown() {
        server.stop(0);
    }

    private static void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        var bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    @Test
    void hentToken_skal_returnere_token_ved_ok_respons() {
        responseStatus = 200;
        responseBody = """
            {"access_token": "eyJ0b2tlbi...", "expires_in": 3600, "token_type": "Bearer"}
            """;

        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", null, null, false);
        var response = klient.hentToken(request);

        assertThat(response.access_token()).isEqualTo("eyJ0b2tlbi...");
        assertThat(response.expires_in()).isEqualTo(3600);
        assertThat(response.token_type()).isEqualTo("Bearer");
    }

    @Test
    void hentToken_skal_sende_korrekt_json_med_authorization_details() {
        responseStatus = 200;
        responseBody = """
            {"access_token": "eyJ0b2tlbi...", "expires_in": 300, "token_type": "Bearer"}
            """;

        var org = new AuthorizationDetails.Consumer("iso6523-actorid-upis", "0192:313367002");
        var authDetails = new AuthorizationDetails("urn:altinn:systemuser", org, List.of("abc-123"), "system_1");
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", "https://resource.no", List.of(authDetails), false);
        klient.hentToken(request);

        assertThat(lastContentType).isEqualTo("application/json");
        assertThat(lastRequestBody).contains("\"identity_provider\":\"maskinporten\"")
            .contains("\"target\":\"https://target.no\"")
            .contains("\"resource\":\"https://resource.no\"")
            .contains("\"type\":\"urn:altinn:systemuser\"")
            .contains("\"ID\":\"0192:313367002\"");
    }

    @Test
    void hentToken_skal_ikke_inkludere_null_felter() {
        responseStatus = 200;
        responseBody = """
            {"access_token": "tok", "expires_in": 60, "token_type": "Bearer"}
            """;

        var request = new HentTokenRequest(IdProvider.TOKENX, "https://target.no", null, null, false);
        klient.hentToken(request);

        assertThat(lastRequestBody).doesNotContain("resource")
            .doesNotContain("authorization_details");
    }

    @Test
    void hentToken_skal_kaste_exception_ved_feil_statuskode() {
        responseStatus = 500;
        responseBody = """
            {"error": "internal server error"}
            """;

        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", null, null, false);
        assertThatThrownBy(() -> klient.hentToken(request))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("F-157385");
    }

    @Test
    void hentToken_skal_kaste_exception_ved_manglende_provider() {
        var request = new HentTokenRequest(null, "https://target.no", null, null, false);
        assertThatThrownBy(() -> klient.hentToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void hentToken_skal_kaste_exception_ved_manglende_target() {
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, null, null, null, false);
        assertThatThrownBy(() -> klient.hentToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void exchangeToken_skal_returnere_token_ved_ok_respons() {
        responseStatus = 200;
        responseBody = """
            {"access_token": "exchanged-token", "expires_in": 1800, "token_type": "Bearer"}
            """;

        var request = new ExchangeTokenRequest(IdProvider.TOKENX, "original-token", "https://target.no");
        var response = klient.exchangeToken(request);

        assertThat(response.access_token()).isEqualTo("exchanged-token");
        assertThat(response.expires_in()).isEqualTo(1800);
        assertThat(response.token_type()).isEqualTo("Bearer");
    }

    @Test
    void exchangeToken_skal_sende_korrekt_json() {
        responseStatus = 200;
        responseBody = """
            {"access_token": "tok", "expires_in": 60, "token_type": "Bearer"}
            """;

        var request = new ExchangeTokenRequest(IdProvider.TOKENX, "bruker-token", "https://target.no");
        klient.exchangeToken(request);

        assertThat(lastContentType).isEqualTo("application/json");
        assertThat(lastRequestBody).contains("\"identity_provider\":\"tokenx\"")
            .contains("\"user_token\":\"bruker-token\"")
            .contains("\"target\":\"https://target.no\"");
    }

    @Test
    void exchangeToken_skal_kaste_exception_ved_feil_statuskode() {
        responseStatus = 400;
        responseBody = """
            {"error": "bad request"}
            """;

        var request = new ExchangeTokenRequest(IdProvider.TOKENX, "ugyldig-token", "https://target.no");
        assertThatThrownBy(() -> klient.exchangeToken(request))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("F-157385");
    }

    @Test
    void exchangeToken_skal_kaste_exception_ved_manglende_provider() {
        var request = new ExchangeTokenRequest(null, "token", "https://target.no");
        assertThatThrownBy(() -> klient.exchangeToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void exchangeToken_skal_kaste_exception_ved_manglende_target() {
        var request = new ExchangeTokenRequest(IdProvider.TOKENX, "token", null);
        assertThatThrownBy(() -> klient.exchangeToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void exchangeToken_skal_kaste_exception_ved_manglende_user_token() {
        var request = new ExchangeTokenRequest(IdProvider.TOKENX, null, "https://target.no");
        assertThatThrownBy(() -> klient.exchangeToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    // --- introspectToken ---

    @Test
    void introspectToken_skal_returnere_aktiv_respons_med_alle_felter() {
        responseStatus = 200;
        responseBody = """
            {
                "active": true,
                "iss": "https://test.maskinporten.no/",
                "exp": 1772098539,
                "iat": 1772097939,
                "nbf": 1772097939,
                "jti": "abc-123",
                "client_id": "klient-id",
                "client_amr": "private_key_jwt",
                "scope": "nav:inntektsmelding/foreldrepenger",
                "token_type": "Bearer",
                "consumer": {
                    "ID": "0192:312651408",
                    "authority": "iso6523-actorid-upis"
                },
                "authorization_details": [{
                    "type": "urn:altinn:systemuser",
                    "system_id": "312651408_navida",
                    "systemuser_id": ["28adb41e-4e93-469d-84db-db41943af6ac"],
                    "systemuser_org": {
                        "ID": "0192:315786940",
                        "authority": "iso6523-actorid-upis"
                    }
                }]
            }
            """;

        var request = new IntrospectTokenRequest(IdProvider.MASKINPORTEN, "eyJhbGciOiJSUzI1NiJ9...");
        var response = klient.introspectToken(request);

        assertThat(response.active()).isTrue();
        assertThat(response.error()).isNull();
        assertThat(response.iss()).isEqualTo("https://test.maskinporten.no/");
        assertThat(response.exp()).isEqualTo(1772098539L);
        assertThat(response.iat()).isEqualTo(1772097939L);
        assertThat(response.nbf()).isEqualTo(1772097939L);
        assertThat(response.jti()).isEqualTo("abc-123");
        assertThat(response.client_id()).isEqualTo("klient-id");
        assertThat(response.client_amr()).isEqualTo("private_key_jwt");
        assertThat(response.scope()).isEqualTo("nav:inntektsmelding/foreldrepenger");
        assertThat(response.token_type()).isEqualTo("Bearer");

        assertThat(response.consumer()).isNotNull();
        assertThat(response.consumer().id()).isEqualTo("0192:312651408");
        assertThat(response.consumer().authority()).isEqualTo("iso6523-actorid-upis");

        assertThat(response.authorization_details()).hasSize(1);
        var auth = response.authorization_details().getFirst();
        assertThat(auth.type()).isEqualTo("urn:altinn:systemuser");
        assertThat(auth.system_id()).isEqualTo("312651408_navida");
        assertThat(auth.systemuser_id()).containsExactly("28adb41e-4e93-469d-84db-db41943af6ac");
        assertThat(auth.systemuser_org().id()).isEqualTo("0192:315786940");
        assertThat(auth.systemuser_org().authority()).isEqualTo("iso6523-actorid-upis");
    }

    @Test
    void introspectToken_skal_returnere_inaktiv_respons_med_feilmelding() {
        responseStatus = 200;
        responseBody = """
            {"active": false, "error": "token is expired"}
            """;

        var request = new IntrospectTokenRequest(IdProvider.ENTRA_ID, "utgatt-token");
        var response = klient.introspectToken(request);

        assertThat(response.active()).isFalse();
        assertThat(response.error()).isEqualTo("token is expired");
        assertThat(response.consumer()).isNull();
        assertThat(response.authorization_details()).isNull();
    }

    @Test
    void introspectToken_skal_sende_korrekt_json() {
        responseStatus = 200;
        responseBody = """
            {"active": true}
            """;

        var request = new IntrospectTokenRequest(IdProvider.TOKENX, "mitt-token");
        klient.introspectToken(request);

        assertThat(lastContentType).isEqualTo("application/json");
        assertThat(lastRequestBody).contains("\"identity_provider\":\"tokenx\"")
            .contains("\"token\":\"mitt-token\"");
    }

    @Test
    void introspectToken_skal_håndtere_entra_id_respons() {
        responseStatus = 200;
        responseBody = """
            {
                "active": true,
                "aud": "api://klient-id",
                "azp": "annen-app",
                "azp_name": "cluster:namespace:annen-app",
                "iss": "https://login.microsoftonline.com/tenant-id/v2.0",
                "sub": "bruker-oid",
                "oid": "bruker-oid",
                "tid": "tenant-id",
                "NAVident": "Z999999",
                "idtyp": "app",
                "groups": ["gruppe-1", "gruppe-2"],
                "roles": ["rolle-1"]
            }
            """;

        var request = new IntrospectTokenRequest(IdProvider.ENTRA_ID, "entra-token");
        var response = klient.introspectToken(request);

        assertThat(response.active()).isTrue();
        assertThat(response.aud()).isEqualTo("api://klient-id");
        assertThat(response.azp()).isEqualTo("annen-app");
        assertThat(response.azp_name()).isEqualTo("cluster:namespace:annen-app");
        assertThat(response.oid()).isEqualTo("bruker-oid");
        assertThat(response.NAVident()).isEqualTo("Z999999");
        assertThat(response.idtyp()).isEqualTo("app");
        assertThat(response.groups()).containsExactly("gruppe-1", "gruppe-2");
        assertThat(response.roles()).containsExactly("rolle-1");
    }

    @Test
    void introspectToken_skal_kaste_exception_ved_feil_statuskode() {
        responseStatus = 503;
        responseBody = """
            {"error": "service unavailable"}
            """;

        var request = new IntrospectTokenRequest(IdProvider.MASKINPORTEN, "et-token");
        assertThatThrownBy(() -> klient.introspectToken(request))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("F-157385");
    }

    @Test
    void introspectToken_skal_kaste_exception_ved_manglende_provider() {
        var request = new IntrospectTokenRequest(null, "et-token");
        assertThatThrownBy(() -> klient.introspectToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void introspectToken_skal_kaste_exception_ved_manglende_token() {
        var request = new IntrospectTokenRequest(IdProvider.MASKINPORTEN, null);
        assertThatThrownBy(() -> klient.introspectToken(request))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void introspectToken_skal_ignorere_ukjente_felter() {
        responseStatus = 200;
        responseBody = """
            {"active": true, "helt_ukjent_felt": "verdi", "enda_et_felt": 42}
            """;

        var request = new IntrospectTokenRequest(IdProvider.ENTRA_ID, "et-token");
        var response = klient.introspectToken(request);

        assertThat(response.active()).isTrue();
    }

    // --- Felles ---

    @Test
    void skal_kaste_teknisk_exception_ved_uoppnåelig_server() {
        var utilgjengeligKlient = new TexasTokenKlient(
            URI.create("http://localhost:1"),
            URI.create("http://localhost:1"),
            URI.create("http://localhost:1")
        );

        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", null, null, false);
        assertThatThrownBy(() -> utilgjengeligKlient.hentToken(request))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("F-432937");
    }
}

