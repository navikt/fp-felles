package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.stream.Collectors.joining;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.http.HttpRequest;
import org.apache.http.client.utils.URIBuilder;

class JerseyUriMapper implements Function<HttpRequest, String> {

    private static final String SLASH = "/";

    @Override
    public String apply(HttpRequest req) {
        String uri = removeQuery(req.getRequestLine().getUri());
        return Arrays.stream(uri.split(SLASH))
                .filter(Predicate.not(this::digits))
                .collect(joining(SLASH));
    }

    private String removeQuery(String uri) {
        try {
            return new URIBuilder(uri).removeQuery().build().toString();
        } catch (URISyntaxException e) {
            return uri;
        }
    }

    private boolean digits(String element) {
        try {
            Long.parseLong(element);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
