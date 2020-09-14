package no.nav.vedtak.sikkerhet.domene;

import java.security.Principal;

import javax.security.auth.Destroyable;

import no.nav.vedtak.util.env.Environment;

public final class ConsumerId implements Principal, Destroyable {
    public static final String SYSTEMUSER_USERNAME = "systembruker.username";

    private static final Environment ENV = Environment.current();

    private String consumerIdString;
    private boolean destroyed;

    public ConsumerId(String consumerId) {
        this.consumerIdString = consumerId;
    }

    public ConsumerId() {
        consumerIdString = ENV.getRequiredProperty(ConsumerId.SYSTEMUSER_USERNAME, () -> new IllegalStateException(
            ConsumerId.SYSTEMUSER_USERNAME + " is not set, failed to set " + ConsumerId.class.getName()));
    }

    @Override
    public void destroy() {
        consumerIdString = null;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String getName() {
        return consumerIdString;
    }

    public String getConsumerId() {
        return consumerIdString;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                (destroyed ? "destroyed" : consumerIdString) +
                "]";
    }
}
