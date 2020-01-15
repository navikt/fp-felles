package no.nav.vedtak.felles.integrasjon.oppgave.v1.request;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Id som genereres fra NAV Aktør Register. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
public class AktørId implements Serializable, Comparable<AktørId> {

    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final AtomicLong DUMMY_AKTØRID = new AtomicLong(1000000000000L);

    @JsonValue
    @NotNull
    @javax.validation.constraints.Pattern(regexp = VALID_REGEXP, message = "aktørId ${validatedValue} har ikke gyldig verdi ( pattern '{regexp}')")
    private String aktørId;  // NOSONAR

    protected AktørId() {
        // for hibernate
    }

    public AktørId(Long aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = validateAktørId(aktørId.toString());
    }

    public AktørId(String aktørId) {
        this.aktørId = validateAktørId(aktørId);
    }

    /**
     * Genererer dummy aktørid unikt for test.
     */
    public static AktørId dummy() {
        return new AktørId(DUMMY_AKTØRID.getAndIncrement());
    }

    private String validateAktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId" + aktørId + ", tillatt pattern: " + VALID_REGEXP);
        }
        return aktørId;
    }

    public String getId() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AktørId)) {
            return false;
        }
        AktørId other = (AktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + aktørId + ">";
    }

    @Override
    public int compareTo(AktørId o) {
        return aktørId.compareTo(o.aktørId);
    }
}
