package no.nav.vedtak.sikkerhet.domene;

import java.security.Principal;

import javax.security.auth.Destroyable;

import no.nav.foreldrepenger.konfig.Environment;

public final class ConsumerId implements Principal, Destroyable {
    public static final String SYSTEMUSER_USERNAME = "systembruker.username";

    private static final Environment ENV = Environment.current();

    private String id;
    private boolean destroyed;

    public ConsumerId(String consumerId) {
        this.id = consumerId;
    }

    public ConsumerId() {
        id = ENV.getRequiredProperty(SYSTEMUSER_USERNAME);
    }

    @Override
    public void destroy() {
        id = null;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String getName() {
        return id;
    }

    public String getConsumerId() {
        return getName();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [id=" + id + ", destroyed=" + destroyed + "]";
    }

}
