package no.nav.vedtak.sikkerhet.context.containers;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;

import javax.security.auth.Destroyable;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Destroyable;

import no.nav.vedtak.sikkerhet.kontekst.Groups;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

public final class SluttBruker implements Principal, Destroyable {

    private String uid;
    private String shortUid;
    private IdentType identType;
    private Set<Groups> grupper;
    private boolean destroyed;

    public SluttBruker(String uid, IdentType identType) {
        this(uid, uid, identType, Set.of());
    }

    public SluttBruker(String uid, String shortUid, IdentType identType, Set<Groups> grupper) {
        this.uid = uid;
        this.shortUid = shortUid;
        this.identType = identType;
        this.grupper = new HashSet<>(grupper);
    }

    public static SluttBruker utledBruker(String uid) {
        return new SluttBruker(uid, IdentType.utledIdentType(uid));
    }

    public IdentType getIdentType() {
        return identType;
    }

    public Set<Groups> getGrupper() {
        return grupper;
    }

    @Override
    public String getName() {
        return uid;
    }

    public String getShortUid() {
        return shortUid;
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
