package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.ws.rs.client.ClientRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import no.nav.vedtak.exception.TekniskException;

class StsAccessTokenClientRequestFilter extends OidcTokenRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(StsAccessTokenClientRequestFilter.class);
    private final StsAccessTokenJerseyClient sts;
    private final Cache<String, String> cache;

    public StsAccessTokenClientRequestFilter(StsAccessTokenJerseyClient sts) {
        this.sts = sts;
        this.cache = cache(1, 55, MINUTES);
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(DEFAULT_NAV_CONSUMERID, sts.getUsername());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    public String accessToken() {
        try {
            return super.accessToken();
        } catch (TekniskException e) {
            return cache.get("systemToken", load());
        }
    }

    private Function<? super String, ? extends String> load() {
        return key -> sts.accessToken();
    }

    private static Cache<String, String> cache(int size, long timeout, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, unit)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner system token fra cache for {} grunnet {}", key, cause);
                    }
                })
                .build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sts=" + sts + ", cache=" + cache + "]";
    }
}
