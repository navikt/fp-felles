package no.nav.vedtak.sikkerhet.abac.pipdata;

import com.fasterxml.jackson.annotation.JsonValue;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Id som genereres fra NAV Aktørregister.
 * Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra DNR til FNR i Folkeregisteret.
 */
public class PipAktørId implements Serializable, Comparable<PipAktørId>, RessursDataValue {
    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @JsonValue
    @NotNull
    @javax.validation.constraints.Pattern(regexp = VALID_REGEXP, message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')")
    private String aktørId;  // NOSONAR

    public PipAktørId(Long aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = validateAktørId(aktørId.toString());
    }

    public PipAktørId(String aktørId) {
        this.aktørId = validateAktørId(aktørId);
    }

    private String validateAktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId '" + aktørId +"', tillatt pattern: "+ VALID_REGEXP);
        }
        return aktørId;
    }

    @Override
    public String getVerdi() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        var other = (PipAktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<maskert>";
    }

    @Override
    public int compareTo(PipAktørId o) {
        // TODO: Burde ikke finnes
        return aktørId.compareTo(o.aktørId);
    }

}
