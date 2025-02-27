package no.nav.vedtak.sikkerhet.jaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.klient.http.CommonHttpHeaders;
import no.nav.vedtak.log.mdc.FnrUtils;
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

    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;

    private AuthenticationFilterDelegate() {
    }

    public static void validerSettKontekst(ResourceInfo resourceInfo, ContainerRequestContext ctx) {
        validerSettKontekst(resourceInfo, ctx, () -> getTokenFromHeader(ctx));
    }

    // Denne Supplier-varianten finnes kun for at k9tilbake skal kunne lete etter tokens i cookies (i tillegg til header)
    public static void validerSettKontekst(ResourceInfo resourceInfo, ContainerRequestContext ctx,
                                           Supplier<Optional<TokenString>> tokenfinder) {
        try {
            Method method = resourceInfo.getResourceMethod();
            var utenAutentiseringRessurs = getAnnotation(resourceInfo, UtenAutentisering.class);
            var metodenavn = method.getName();
            if (KontekstHolder.harKontekst()) {
                LOG.info("Kall til {} hadde kontekst {}", metodenavn, KontekstHolder.getKontekst().getKompaktUid());
                KontekstHolder.fjernKontekst();
            }
            MDC.clear();
            setCallAndConsumerId(ctx);
            LOG.trace("{} i klasse {}", metodenavn, method.getDeclaringClass());
            // Kan vurdere å unnta metodenavn = getOpenApi og getDeclaringClass startsWith io.swagger + endsWith OpenApiResource
            if (utenAutentiseringRessurs.isPresent()) {
                KontekstHolder.setKontekst(BasisKontekst.ikkeAutentisertRequest(MDCOperations.getConsumerId()));
                LOG.trace("{} er whitelisted", metodenavn);
            } else {
                var tokenString = tokenfinder.get().orElseThrow(() -> new ValideringsFeil("Mangler token"));
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
        String callId = getHeader(request, CommonHttpHeaders.HEADER_NAV_CALLID)
            .or(() -> getHeader(request, CommonHttpHeaders.HEADER_NAV_ALT_CALLID))
            .or(() -> getHeader(request, CommonHttpHeaders.HEADER_NAV_LOWER_CALL_ID))
            .orElseGet(MDCOperations::generateCallId);
        MDCOperations.putCallId(callId);

        getHeader(request, CommonHttpHeaders.HEADER_NAV_CONSUMER_ID).ifPresent(MDCOperations::putConsumerId);
    }

    private static Optional<String> getHeader(ContainerRequestContext request, String header) {
        return Optional.ofNullable(request.getHeaderString(header)).filter(s -> !s.isEmpty());
    }

    private static void setUserAndConsumerId(String subject) {
        Optional.ofNullable(subject).ifPresent(MDCOperations::putUserId);
        if (MDCOperations.getConsumerId() == null && subject != null) {
            MDCOperations.putConsumerId(FnrUtils.maskFnr(subject));
        }
    }

    private static <T extends Annotation> Optional<T> getAnnotation(ResourceInfo resourceInfo, Class<T> tClass) {
        return Optional.ofNullable(resourceInfo.getResourceMethod().getAnnotation(tClass))
            .or(() -> Optional.ofNullable(resourceInfo.getResourceClass().getAnnotation(tClass)));
    }

    public static Optional<TokenString> getTokenFromHeader(ContainerRequestContext request) {
        return Optional.ofNullable(request.getHeaderString(AUTHORIZATION_HEADER))
            .filter(headerValue -> headerValue.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE))
            .map(headerValue -> headerValue.substring(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE.length()))
            .map(TokenString::new);
    }

    private static void validerTokenSetKontekst(TokenString tokenString) {
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
                validateResult.identType(), token, validateResult.oid(), validateResult.getGrupper()));
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
