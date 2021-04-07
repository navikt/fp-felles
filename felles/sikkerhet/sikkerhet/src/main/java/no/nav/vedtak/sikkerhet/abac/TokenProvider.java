package no.nav.vedtak.sikkerhet.abac;

import java.net.URI;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.sikkerhet.abac.AbacIdToken.TokenType;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public interface TokenProvider {

    default String getUid() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    default String userToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    default String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();
    }

    default TokenType oidcTokenType() {
        try {
            return URI.create(SignedJWT.parse(userToken())
                    .getJWTClaimsSet().getIssuer()).getHost().contains("tokendings") ? TokenType.TOKENX : TokenType.OIDC;

        } catch (Exception e) {
            throw new IllegalArgumentException("Ukjent token type");
        }
    }

}
