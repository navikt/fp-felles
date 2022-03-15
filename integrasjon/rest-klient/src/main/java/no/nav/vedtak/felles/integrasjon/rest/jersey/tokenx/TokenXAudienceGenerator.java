package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.Namespace;

public class TokenXAudienceGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(TokenXAudienceGenerator.class);
    private static final Environment ENV = Environment.current();

    public TokenXAudience audience(URI uri) {
        LOG.trace("Utleder audience for {}", uri);
        String host = uri.getHost();
        var elems = host.split("\\.");

        if (elems.length == 1) {
            return new TokenXAudience(cluster(host), ENV.getNamespace(), elems[0]);
        }
        if (elems.length == 2) {
            return new TokenXAudience(cluster(host), Namespace.of(elems[1]), elems[0]);
        }
        throw new IllegalArgumentException("Kan ikke analysere " + host + "(" + elems.length + ")");
    }

    private Cluster cluster(String key) {
        return Optional.ofNullable(ENV.getProperty(key))
                .map(Cluster::of)
                .orElseGet(ENV::getCluster);
    }
}
