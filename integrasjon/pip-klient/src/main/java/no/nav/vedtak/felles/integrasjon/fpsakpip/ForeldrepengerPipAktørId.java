package no.nav.vedtak.felles.integrasjon.fpsakpip;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Id som genereres fra NAV Aktørregister.
 * Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra DNR til FNR i Folkeregisteret.
 */
public class ForeldrepengerPipAktørId implements Serializable, Comparable<ForeldrepengerPipAktørId> {
    private static final String VALID_REGEXP = "^\\d{13}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    @JsonValue
    @NotNull
    @jakarta.validation.constraints.Pattern(regexp = VALID_REGEXP, message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')")
    private String aktørId;

    public ForeldrepengerPipAktørId(Long aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        this.aktørId = validateAktørId(aktørId.toString());
    }

    public ForeldrepengerPipAktørId(String aktørId) {
        this.aktørId = validateAktørId(aktørId);
    }

    private String validateAktørId(String aktørId) {
        Objects.requireNonNull(aktørId, "aktørId");
        if (!VALID.matcher(aktørId).matches()) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ugyldig aktørId '" + aktørId + "', tillatt pattern: " + VALID_REGEXP);
        }
        return aktørId;
    }

    public String getVerdi() {
        return aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForeldrepengerPipAktørId annen && Objects.equals(aktørId, annen.aktørId);
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
    public int compareTo(ForeldrepengerPipAktørId o) {
        // TODO: Burde ikke finnes
        return aktørId.compareTo(o.aktørId);
    }

}
