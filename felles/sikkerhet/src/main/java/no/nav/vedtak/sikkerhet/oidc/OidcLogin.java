package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class OidcLogin {

    public enum LoginResult {
        SUCCESS,
        ID_TOKEN_MISSING,
        ID_TOKEN_EXPIRED,
        ID_TOKEN_INVALID
    }

    public record Resultat(LoginResult loginResult,
                           String subject,
                           String errorMessage) {}

    public static Resultat validerToken(OpenIDToken openIDToken) {
        if (openIDToken == null || openIDToken.provider() == null) {
            return new Resultat(LoginResult.ID_TOKEN_MISSING, null, null);
        }
        var tokenValidator = OidcTokenValidatorConfig.instance().getValidator(openIDToken.provider());
        var validateResult = tokenValidator.validate(openIDToken.primary());
        if (needToRefreshToken(openIDToken, validateResult, tokenValidator)) {
            return new Resultat(LoginResult.ID_TOKEN_EXPIRED, null, null);
        }
        if (validateResult.isValid()) {
            return new Resultat(LoginResult.SUCCESS, validateResult.getSubject(), null);
        }
        return new Resultat(LoginResult.ID_TOKEN_INVALID, null, validateResult.getErrorMessage());
    }

    private static boolean needToRefreshToken(OpenIDToken token, OidcTokenValidatorResult validateResult, OidcTokenValidator tokenValidator) {
        return !validateResult.isValid() && tokenValidator.validateWithoutExpirationTime(token.primary()).isValid();
    }

}
