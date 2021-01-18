package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import java.util.Optional;
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

public class StsAccessTokenClientRequestFilter extends OidcTokenRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(StsAccessTokenClientRequestFilter.class);
    private final StsAccessTokenJerseyClient sts;
    private final Cache<String, String> cache;
    private final String tema;

    public StsAccessTokenClientRequestFilter(StsAccessTokenJerseyClient sts, String tema) {
        this.sts = sts;
        this.cache = cache(1, 55, MINUTES);
        this.tema = tema;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(DEFAULT_NAV_CONSUMERID, sts.getUsername());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
        ctx.getHeaders().add(TEMA, tema);
    }

    @Override
    public String accessToken() {
        return Optional.ofNullable(suppliedToken())
                .orElseGet(() -> systemToken());
    }

    private String systemToken() {
        return Optional.ofNullable(samlToken())
                .map(t -> cache.get("systemToken", load()))
                        .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    private Function<? super String, ? extends String> load() {
        LOG.info("Oppdaterer cache med system token");
        return key -> sts.accessToken();
    }

    private static Cache<String, String> cache(int size, long timeout, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, unit)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner system token fra cache grunnet {}", cause);
                    }
                })
                .build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [sts=" + sts + ", cache=" + cache + ", tema=" + tema + "]";
    }
}
