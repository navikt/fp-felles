package no.nav.vedtak.sikkerhet.context.containers;

import java.security.Principal;
import java.util.regex.Pattern;

import javax.security.auth.Destroyable;

public final class SluttBruker implements Principal, Destroyable {

    private static final Pattern VALID_AKTØRID = Pattern.compile("^\\d{13}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_PERSONIDENT = Pattern.compile("^\\d{11}$", Pattern.CASE_INSENSITIVE);


    private String uid;
    private IdentType identType;
    private boolean destroyed;

    public SluttBruker(String uid, IdentType identType) {
        this.uid = uid;
        this.identType = identType;
    }

    public static SluttBruker internBruker(String uid) {
        if (ConsumerId.SYSTEMUSER_USERNAME.equals(uid)) {
            return new SluttBruker(uid, IdentType.Prosess);
        } else if (uid != null && (VALID_AKTØRID.matcher(uid).matches() || VALID_PERSONIDENT.matcher(uid).matches())) {
            return new SluttBruker(uid, IdentType.EksternBruker);
        }
        // Evt sjekke på kjente interne ident-patterns og systembruker-patterns
        return new SluttBruker(uid, IdentType.InternBruker);
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
