package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.util.StringJoiner;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Namespace;

public record TokenXAudience(Cluster cluster, Namespace namespace, String app) {

    public String asAudience() {
        var joiner = new StringJoiner(":");
        joiner.add(cluster.clusterName());
        joiner.add(namespace.getName());
        joiner.add(app);
        var audience = joiner.toString();
        return audience;
    }
}
