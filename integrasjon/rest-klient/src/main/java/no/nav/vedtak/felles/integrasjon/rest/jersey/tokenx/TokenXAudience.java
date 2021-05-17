package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Namespace;

public record TokenXAudience(Cluster cluster, Namespace namespace, String app) {

    private static final Logger LOG = LoggerFactory.getLogger(TokenXAudience.class);
    public String asAudience() {
        var joiner = new StringJoiner(":");
        joiner.add(cluster.clusterName());
        joiner.add(namespace.getName());
        joiner.add(app);
        var audience = joiner.toString();
        LOG.trace("Audience er {}", audience);
        return audience;
    }
}
