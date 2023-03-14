package no.nav.vedtak.sikkerhet.loginmodule.oidc;

import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorResult;

public class OidcLogin {

    public enum LoginResult {
        SUCCESS,
        ID_TOKEN_MISSING,
        ID_TOKEN_EXPIRED,
        ID_TOKEN_INVALID
    }

    public record Resultat(LoginResult loginResult, SluttBruker subject, String errorMessage) {
    }

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
            return new Resultat(LoginResult.SUCCESS,
                new SluttBruker(validateResult.getSubject(), validateResult.getCompactSubject(), validateResult.getIdentType()), null);
        }
        return new Resultat(LoginResult.ID_TOKEN_INVALID, null, validateResult.getErrorMessage());
    }

    private static boolean needToRefreshToken(OpenIDToken token, OidcTokenValidatorResult validateResult, OidcTokenValidator tokenValidator) {
        return !validateResult.isValid() && tokenValidator.validateWithoutExpirationTime(token.primary()).isValid();
    }

}
