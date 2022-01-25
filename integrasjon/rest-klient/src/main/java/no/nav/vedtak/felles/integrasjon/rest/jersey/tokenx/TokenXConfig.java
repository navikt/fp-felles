package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.text.ParseException;

import com.nimbusds.jose.jwk.RSAKey;

import no.nav.foreldrepenger.konfig.Environment;

public record TokenXConfig(String clientId, String privateJwk, RSAKey privateKey) {

    private static final Environment ENV = Environment.current();

    static TokenXConfig fraEnv() {
        var jwk =  ENV.getRequiredProperty("token.x.private.jwk");
        return new TokenXConfig(
                ENV.getRequiredProperty("token.x.client.id"),
                jwk, rsaKey(jwk));
    }

    private static RSAKey rsaKey(String privateJwk) {
        try {
            return RSAKey.parse(privateJwk);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [clientId=" + clientId + "]";
    }
}
