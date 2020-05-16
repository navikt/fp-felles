package no.nav.vedtak.felles.integrasjon.jms;

import no.nav.vedtak.util.env.Cluster;
import no.nav.vedtak.util.env.Environment;

public class ToggleJms {

    public static final String TOGGLE_JMS = "felles.jms";

    private static final Environment ENV = Environment.current();

    private final boolean enabled;

    public ToggleJms() {
        boolean clusterDefault = !Cluster.LOCAL.equals(ENV.getCluster());
        String jmsEnabled = ENV.getProperty(TOGGLE_JMS, Boolean.toString(clusterDefault));
        this.enabled = Boolean.parseBoolean(jmsEnabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabled() {
        return !isEnabled();
    }
}
