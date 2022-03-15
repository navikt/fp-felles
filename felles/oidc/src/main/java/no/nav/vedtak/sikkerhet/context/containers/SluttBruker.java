package no.nav.vedtak.sikkerhet.context.containers;

import java.security.Principal;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.security.auth.Destroyable;

public final class SluttBruker implements Principal, Destroyable {

    private static final Pattern VALID_AKTØRID = Pattern.compile("^\\d{13}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_PERSONIDENT = Pattern.compile("^\\d{11}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_ANSATTIDENT = Pattern.compile("^\\w\\d{6}$", Pattern.CASE_INSENSITIVE);


    private String uid;
    private IdentType identType;
    private boolean destroyed;

    public SluttBruker(String uid, IdentType identType) {
        this.uid = uid;
        this.identType = identType;
    }

    public static SluttBruker utledBruker(String uid) {
        return new SluttBruker(uid, utledIdentType(uid));
    }

    public static SluttBruker lokalSystembrukerProsess() {
        return new SluttBruker(ConsumerId.SYSTEMUSER_USERNAME, IdentType.Prosess);
    }

    private static IdentType utledIdentType(String uid) {
        if (Objects.equals(ConsumerId.SYSTEMUSER_USERNAME, uid)) {
            return IdentType.Systemressurs;
        } else if (uid != null && (VALID_AKTØRID.matcher(uid).matches() || VALID_PERSONIDENT.matcher(uid).matches())) {
            return IdentType.EksternBruker;
        } else if (uid != null && uid.startsWith("srv")) {
            return IdentType.Systemressurs;
        } else if (uid != null && VALID_ANSATTIDENT.matcher(uid).matches()) {
            return IdentType.InternBruker;
        }
        // TODO - her skal det strengt tatt være en exception .... Skal på sikt brukes til oppførsel for tokenprovider
        return IdentType.InternBruker;
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
