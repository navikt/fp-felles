package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenClient;

class StsAccessTokenClientRequestFilter implements ClientRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(StsAccessTokenClientRequestFilter.class);
    private final StsAccessTokenClient sts;
    private final Cache<String, String> cache;

    public StsAccessTokenClientRequestFilter(StsAccessTokenClient sts) {
        this.sts = sts;
        this.cache = cache(1, 55, MINUTES);
    }

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(DEFAULT_NAV_CONSUMERID, sts.getUsername());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + oidcToken());
    }

    private synchronized String oidcToken() {
        return cache.get("systemToken", load());
    }

    private Function<? super String, ? extends String> load() {
        return key -> sts.hentAccessToken();
    }

    private static Cache<String, String> cache(int size, long timeout, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, unit)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner {} for {} grunnet {}", value, key, cause);
                    }
                })
                .build();
    }
}
