package no.nav.vedtak.sikkerhet.context.containers;

import no.nav.foreldrepenger.konfig.Environment;

import javax.security.auth.Destroyable;
import java.security.Principal;

public final class ConsumerId implements Principal, Destroyable {


    private static final Environment ENV = Environment.current();
    public static final String SYSTEMUSER_USERNAME_PROPERTY = "systembruker.username";
    public static final String SYSTEMUSER_USERNAME = ENV.getProperty(SYSTEMUSER_USERNAME_PROPERTY);

    private String id;
    private boolean destroyed;

    public ConsumerId(String consumerId) {
        this.id = consumerId;
    }

    public ConsumerId() {
        id = SYSTEMUSER_USERNAME;
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
