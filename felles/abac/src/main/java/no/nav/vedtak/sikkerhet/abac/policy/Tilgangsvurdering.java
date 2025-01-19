package no.nav.vedtak.sikkerhet.abac.policy;

import java.util.Set;

import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

public record Tilgangsvurdering(AbacResultat abacResultat, String årsak, Set<AnsattGruppe> kreverGrupper) {

    public boolean fikkTilgang() {
        return abacResultat == AbacResultat.GODKJENT;
    }

    public static Tilgangsvurdering godkjenn() {
        return new Tilgangsvurdering(AbacResultat.GODKJENT, "", Set.of());
    }

    public static Tilgangsvurdering godkjenn(AnsattGruppe kreverGruppe) {
        return new Tilgangsvurdering(AbacResultat.GODKJENT, "", Set.of(kreverGruppe));
    }

    public static Tilgangsvurdering godkjenn(Set<AnsattGruppe> kreverGrupper) {
        return new Tilgangsvurdering(AbacResultat.GODKJENT, "", kreverGrupper);
    }

    public static Tilgangsvurdering avslåGenerell(String årsak) {
        return new Tilgangsvurdering(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK, årsak, Set.of());
    }
}
