package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.nimbusds.jwt.SignedJWT;

public class SystemTokenClientRequestFilter implements ClientRequestFilter {

    private static final Duration CACHE_DURATION = Duration.ofMinutes(55);
    private static final Logger LOG = LoggerFactory.getLogger(SystemTokenClientRequestFilter.class);
    private final StsAccessTokenJerseyClient sts;
    private final LoadingCache<String, String> cache;
    private final String tema;

    public SystemTokenClientRequestFilter(StsAccessTokenJerseyClient sts, String tema) {
        this(sts, tema, cache(1, CACHE_DURATION, sts));
    }

    public SystemTokenClientRequestFilter(StsAccessTokenJerseyClient sts, String tema, LoadingCache<String, String> cache) {
        this.sts = sts;
        this.cache = cache;
        this.tema = tema;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemToken());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + systemToken());
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
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner system token fra cache grunnet {}", cause);
                    }
                })
                .build(k -> load(sts, duration));
    }

    private static String load(StsAccessTokenJerseyClient sts, Duration duration) {
        LOG.info("Oppdaterer cache med system token, cache innslag er gyldig til {}", format(now().plus(duration)));
        var token = sts.accessToken();
        LOG.info("System token er gyldig til {}", format(expiry()));
        return token;
    }

    private static String format(LocalDateTime d) {
        return Optional.ofNullable(d)
                .map(ISO_LOCAL_DATE_TIME::format)
                .orElse("<null>");
    }

    private static LocalDateTime expiry() {
        try {
            return SignedJWT.parse(getSubjectHandler()
                    .getInternSsoToken())
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
