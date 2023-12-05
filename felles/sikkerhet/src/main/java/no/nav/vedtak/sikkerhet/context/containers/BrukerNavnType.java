package no.nav.vedtak.sikkerhet.context.containers;

import java.security.Principal;

import javax.security.auth.Destroyable;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;

public final class BrukerNavnType implements Principal, Destroyable {

    private String uid;
    private final IdentType identType;
    private boolean destroyed;

    public BrukerNavnType(String uid, IdentType identType) {
        this.uid = uid;
        this.identType = identType;
    }

    public BrukerNavnType(BrukerNavnType brukerNavnType) {
        this.uid = brukerNavnType.uid;
        this.identType = brukerNavnType.identType;
    }

    public IdentType getIdentType() {
        return identType;
    }

    @Override
    public String getName() {
        return uid;
    }


    @Override
    public void destroy() {
        uid = null;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
            "identType=" + identType + ", " +
            "uid=" + (destroyed ? "destroyed" : uid) +
            "]";
    }
}
