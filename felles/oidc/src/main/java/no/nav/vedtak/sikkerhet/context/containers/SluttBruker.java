package no.nav.vedtak.sikkerhet.context.containers;

import java.security.Principal;

import javax.security.auth.Destroyable;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.Systembruker;

public final class SluttBruker implements Principal, Destroyable {

    private String uid;
    private IdentType identType;
    private boolean destroyed;

    public SluttBruker(String uid, IdentType identType) {
        this.uid = uid;
        this.identType = identType;
    }

    public static SluttBruker utledBruker(String uid) {
        return new SluttBruker(uid, IdentType.utledIdentType(uid));
    }

    public static SluttBruker lokalSystembrukerProsess() {
        return new SluttBruker(Systembruker.username(), IdentType.Prosess);
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
