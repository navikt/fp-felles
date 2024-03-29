package no.nav.vedtak.sikkerhet.jaxrs;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;

/**
 * Bruksanvisning inntil alle er over og det evt samles her:
 * - App må lage et filter som implementerer ContainerRequestFilter
 * - Filter må annotert med Provider og legges inn i Application/getClasses()
 * - Legg på @Context private ResourceInfo resourceinfo
 * - Kall AuthenticationFilterDelegate . validerSettKontekst og la evt exceptions passere ut til Jersey
 * <p>
 * App må også ha et ContainerResponseFilter som kaller AuthenticationFilterDelegate . fjernKontekst
 */
public class AuthenticationFilterDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilterDelegate.class);

    private static final String ID_TOKEN_COOKIE_NAME = "ID_token";
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;

    private AuthenticationFilterDelegate() {
    }


    public static void validerSettKontekst(ResourceInfo resourceInfo, ContainerRequestContext ctx) {
        validerSettKontekst(resourceInfo, ctx, null);
    }

    public static void validerSettKontekst(ResourceInfo resourceInfo, ContainerRequestContext ctx, String cookiePath) {
        try {
            Method method = resourceInfo.getResourceMethod();
            var utenAutentiseringRessurs = method.getAnnotation(UtenAutentisering.class);
            var metodenavn = method.getName();
            if (KontekstHolder.harKontekst()) {
                LOG.info("Kall til {} hadde kontekst {}", metodenavn, KontekstHolder.getKontekst().getKompaktUid());
                KontekstHolder.fjernKontekst();
            }
            MDC.clear();
            setCallAndConsumerId(ctx);
            LOG.trace("{} i klasse {}", metodenavn, method.getDeclaringClass());
            // Kan vurdere å unnta metodenavn = getOpenApi og getDeclaringClass startsWith io.swagger + endsWith OpenApiResource
            if (utenAutentiseringRessurs != null ) {
                KontekstHolder.setKontekst(BasisKontekst.ikkeAutentisertRequest(MDCOperations.getConsumerId()));
                LOG.trace("{} er whitelisted", metodenavn);
            } else {
                var tokenString = getToken(ctx, cookiePath)
                    .orElseThrow(() -> new ValideringsFeil("Mangler token"));
                validerTokenSetKontekst(tokenString);
                setUserAndConsumerId(KontekstHolder.getKontekst().getUid());
            }
        } catch (TekniskException | TokenFeil e) {
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.UNAUTHORIZED);
        }
    }

    public static void fjernKontekst() {
        if (KontekstHolder.harKontekst()) {
            KontekstHolder.fjernKontekst();
        }
        MDC.clear();
    }

    private static void setCallAndConsumerId(ContainerRequestContext request) {
        String callId = Optional.ofNullable(request.getHeaderString(MDCOperations.HTTP_HEADER_CALL_ID))
            .or(() -> Optional.ofNullable(request.getHeaderString(MDCOperations.HTTP_HEADER_ALT_CALL_ID)))
            .orElseGet(MDCOperations::generateCallId);
        MDCOperations.putCallId(callId);

        Optional.ofNullable(request.getHeaderString(MDCOperations.HTTP_HEADER_CONSUMER_ID))
            .ifPresent(MDCOperations::putConsumerId);
    }

    private static void setUserAndConsumerId(String subject) {
        Optional.ofNullable(subject).ifPresent(MDCOperations::putUserId);
        if (MDCOperations.getConsumerId() == null && subject != null) {
            MDCOperations.putConsumerId(subject);
        }
    }

    private static Optional<TokenString> getToken(ContainerRequestContext request, String cookiePath) {
        return getTokenFromHeader(request).or(() -> getCookieToken(request, cookiePath));
    }

    private static Optional<TokenString> getTokenFromHeader(ContainerRequestContext request) {
        String headerValue = request.getHeaderString(AUTHORIZATION_HEADER);
        return headerValue != null && headerValue.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE)
            ? Optional.of(new TokenString(headerValue.substring(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE.length())))
            : Optional.empty();
    }

    private static Optional<TokenString> getCookieToken(ContainerRequestContext request, String cookiePath) {
        var idTokenCookie = Optional.ofNullable(request.getCookies()).map(c -> c.get(ID_TOKEN_COOKIE_NAME));
        return idTokenCookie.filter(c -> cookiePath != null && cookiePath.equalsIgnoreCase(c.getPath()))
            .or(() -> idTokenCookie)
            .map(Cookie::getValue)
            .map(TokenString::new);
    }

    public static void validerTokenSetKontekst(TokenString tokenString) {
        // Sett opp OpenIDToken
        var claims = JwtUtil.getClaims(tokenString.token());
        var configuration = ConfigProvider.getOpenIDConfiguration(JwtUtil.getIssuer(claims))
            .orElseThrow(() -> new TokenFeil("Token mangler issuer claim"));
        var expiresAt = Optional.ofNullable(JwtUtil.getExpirationTime(claims)).orElseGet(() -> Instant.now().plusSeconds(300));
        var token = new OpenIDToken(configuration.type(), OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE, tokenString, null, expiresAt.toEpochMilli());

        // Valider
        var tokenValidator = OidcTokenValidatorConfig.instance().getValidator(token.provider());
        var validateResult = tokenValidator.validate(token.primary());

        // Håndter valideringsresultat
        if (validateResult.isValid()) {
            KontekstHolder.setKontekst(RequestKontekst.forRequest(validateResult.subject(), validateResult.compactSubject(),
                validateResult.identType(), token, validateResult.getGrupper()));
            LOG.trace("token validert");
        } else {
            throw new ValideringsFeil("Ugyldig token");
        }
    }

    private static class TokenFeil extends RuntimeException {
        TokenFeil(String message) {
            super(message);
        }
    }

    private static class ValideringsFeil extends RuntimeException {
        ValideringsFeil(String message) {
            super(message);
        }
    }

}
