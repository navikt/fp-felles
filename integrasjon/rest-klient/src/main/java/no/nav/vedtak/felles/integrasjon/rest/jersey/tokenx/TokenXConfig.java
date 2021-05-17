package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;
import java.text.ParseException;

import com.nimbusds.jose.jwk.RSAKey;

import no.nav.foreldrepenger.konfig.Environment;

public record TokenXConfig(URI wellKnownUrl, String clientId, String privateJwk) {

    private static final Environment ENV = Environment.current();

    static TokenXConfig fraEnv() {
        return new TokenXConfig(ENV.getRequiredProperty("token.x.well.known.url", URI.class),
                ENV.getRequiredProperty("token.x.client.id"),
                ENV.getRequiredProperty("token.x.private.jwk"));
    }

    public RSAKey rsaKey() {
        try {
            return RSAKey.parse(privateJwk);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [wellKnownUrl=" + wellKnownUrl + ",clientId=" + clientId + "]";
    }
}
