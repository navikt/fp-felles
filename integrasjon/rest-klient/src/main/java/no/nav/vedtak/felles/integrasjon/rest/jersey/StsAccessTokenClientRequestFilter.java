package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.exception.TekniskException;

public class StsAccessTokenClientRequestFilter extends OidcTokenRequestFilter {

    private static final Duration CACHE_DURATION = Duration.ofMinutes(55);
    private static final Logger LOG = LoggerFactory.getLogger(StsAccessTokenClientRequestFilter.class);
    private final StsAccessTokenJerseyClient sts;
    private final LoadingCache<String, String> cache;
    private final String tema;

    public StsAccessTokenClientRequestFilter(StsAccessTokenJerseyClient sts, String tema) {
        this(sts, tema, cache(1, CACHE_DURATION, sts));
    }

    public StsAccessTokenClientRequestFilter(StsAccessTokenJerseyClient sts, String tema, LoadingCache<String, String> cache) {
        this.sts = sts;
        this.cache = cache;
        this.tema = tema;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemToken());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    protected String exchangedToken() {
        return Optional.ofNullable(samlToken())
                .map(t -> systemToken())
                .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    private String systemToken() {
        return cache.get("systemToken");
    }

    private static LoadingCache<String, String> cache(int size, Duration duration, StsAccessTokenJerseyClient sts) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String token, RemovalCause cause) {
                        LOG.info("Fjerner system token som utgår {} fra cache grunnet {}", expiry(token), cause);
                    }
                })
                .build(k -> load(sts));
    }

    private static String load(StsAccessTokenJerseyClient sts) {
        LOG.info("Oppdaterer cache med system token");
        var token = sts.accessToken();
        LOG.info("System token er gyldig til {}", format(expiry(token)));
        return token;
    }

    private static String format(LocalDateTime d) {
        return Optional.ofNullable(d)
                .map(ISO_LOCAL_DATE_TIME::format)
                .orElse("<null>");
    }

    private static LocalDateTime expiry(String token) {
        try {
            return SignedJWT.parse(token)
                    .getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            LOG.trace("Kunne ikke hente expiration dato fra token", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sts=" + sts + ", cache=" + cache + ", tema=" + tema + "]";
    }

}
