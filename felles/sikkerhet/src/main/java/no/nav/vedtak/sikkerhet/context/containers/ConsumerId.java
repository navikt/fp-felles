package no.nav.vedtak.sikkerhet.context.containers;

import no.nav.vedtak.sikkerhet.kontekst.Systembruker;

import javax.security.auth.Destroyable;
import java.security.Principal;

public final class ConsumerId implements Principal, Destroyable {

    private String id;
    private boolean destroyed;

    public ConsumerId(String consumerId) {
        this.id = consumerId;
    }

    public ConsumerId() {
        this(Systembruker.username());
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
