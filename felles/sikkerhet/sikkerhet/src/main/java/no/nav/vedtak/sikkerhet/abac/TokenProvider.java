package no.nav.vedtak.sikkerhet.abac;

import java.text.ParseException;

import com.nimbusds.jwt.SignedJWT;

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

    default String oidcTokenType() {

        try {
            return SignedJWT.parse(userToken())
                    .getJWTClaimsSet().getIssuer();

        } catch (ParseException e) {
            throw new IllegalArgumentException("Ukjent token type");
        }
    }

}
