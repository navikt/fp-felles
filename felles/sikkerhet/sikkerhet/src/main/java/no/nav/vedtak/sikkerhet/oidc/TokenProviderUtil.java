package no.nav.vedtak.sikkerhet.oidc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class TokenProviderUtil {

    private TokenProviderUtil() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

    /**
     * @param tokenRequestSupplier tThe supplier for the token request
     * @param tokenExtractor       The function to extract the required token(s)
     *                             from the response
     * @param <T>                  The returned token class
     * @return The token from the response, or <tt>Exception</tt> if the status code
     *         from the response indicates a client error (4xx).
     */
    public static <T> T getToken(Supplier<HttpRequestBase> tokenRequestSupplier, Function<String, T> tokenExtractor) {
        return getTokenInternal(tokenRequestSupplier, tokenExtractor, false);
    }

    /**
     * @param tokenRequestSupplier tThe supplier for the token request
     * @param tokenExtractor       The function to extract the required token(s)
     *                             from the response
     * @param <T>                  The returned token class
     * @return The token from the response, or <tt>Optional.empty</tt> if the status
     *         code from the response indicates a client error (4xx). The expected
     *         4xx error is typically when the refresh token has expired.
     */
    public static <T> Optional<T> getTokenOptional(Supplier<HttpRequestBase> tokenRequestSupplier, Function<String, T> tokenExtractor) {
        return Optional.ofNullable(getTokenInternal(tokenRequestSupplier, tokenExtractor, true));
    }

    private static <T> T getTokenInternal(Supplier<HttpRequestBase> tokenRequestSupplier, Function<String, T> tokenExtractor,
            boolean foventer400Koder) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpRequestBase request = tokenRequestSupplier.get();
            try (CloseableHttpResponse response = client.execute(request)) {
                String responseString = responseText(response);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return tokenExtractor.apply(responseString);
                }
                if (400 <= statusCode && statusCode < 500 && foventer400Koder) {
                    return null;
                }
                throw TokenProviderFeil.kunneIkkeHenteTokenFikk40xKode(statusCode, responseString);
            } finally {
                request.reset();
            }
        } catch (IOException e) {
            throw TokenProviderFeil.kunneIkkeHenteTokenFikkIOException(e);
        }
    }

    private static String responseText(CloseableHttpResponse response) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
            try (BufferedReader br = new BufferedReader(isr)) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    static String findToken(String responseString, String tokenName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode json = mapper.readTree(responseString);
            JsonNode token = json.get(tokenName);
            if (token == null) {
                throw TokenProviderFeil.fikkIkkeTokenIReponse(tokenName);
            }
            return token.textValue();
        } catch (IOException e) {
            throw TokenProviderFeil.kunneIkkeHenteTokenFikkIOException(e);
        }
    }

    static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8));
    }

}
