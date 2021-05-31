package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.http.HttpRequest;

class JerseyUriMapper implements Function<HttpRequest, String> {

    private static final String SLASH = "/";

    @Override
    public String apply(HttpRequest req) {
        return Arrays.stream(req.getRequestLine().getUri().split(SLASH))
                .filter(Predicate.not(this::digits))
                .collect(joining(SLASH));
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
