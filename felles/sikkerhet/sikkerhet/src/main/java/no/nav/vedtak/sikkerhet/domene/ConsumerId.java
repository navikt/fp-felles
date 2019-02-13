package no.nav.vedtak.sikkerhet.domene;

import no.nav.vedtak.konfig.PropertyUtil;
import no.nav.vedtak.sts.client.SecurityConstants;

import javax.security.auth.Destroyable;
import java.security.Principal;

public final class ConsumerId implements Principal, Destroyable {

    private String consumerIdString;
    private boolean destroyed;

    public ConsumerId(String consumerId) {
        this.consumerIdString = consumerId;
    }

    public ConsumerId() {
        consumerIdString = PropertyUtil.getProperty(SecurityConstants.SYSTEMUSER_USERNAME);

        if (consumerIdString == null) {
            throw new IllegalStateException(
                    SecurityConstants.SYSTEMUSER_USERNAME + " is not set, failed to set " + ConsumerId.class.getName());
        }
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
