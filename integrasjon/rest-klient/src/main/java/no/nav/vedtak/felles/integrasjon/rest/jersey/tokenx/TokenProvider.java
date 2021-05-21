package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

public interface TokenProvider {

    static final Logger LOG = LoggerFactory.getLogger(TokenProvider.class);

    String getToken();

    default boolean isTokenX() {
        try {
            return URI.create(SignedJWT.parse(getToken())
                    .getJWTClaimsSet().getIssuer()).getHost().contains("tokendings");

        } catch (Exception e) {
            LOG.warn("Kunne ikke sjekke token issuer", e);
            return false;
        }
    }
}
