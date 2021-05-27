package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.util.function.Function;

import org.apache.http.HttpRequest;

class JerseyUriMapper implements Function<HttpRequest, String> {

    @Override
    public String apply(HttpRequest req) {
        return req.getRequestLine().getUri();
    }

}
