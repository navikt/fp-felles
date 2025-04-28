package no.nav.vedtak.sikkerhet.abac.policy;

import java.util.Set;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

public record Tilgangsvurdering(TilgangResultat tilgangResultat, String årsak, Set<AnsattGruppe> kreverGrupper, String auditIdent) {

    public boolean fikkTilgang() {
        return tilgangResultat.fikkTilgang();
    }

    public static Tilgangsvurdering godkjenn() {
        return new Tilgangsvurdering(TilgangResultat.GODKJENT, "", Set.of(), null);
    }

    public static Tilgangsvurdering godkjenn(AnsattGruppe kreverGruppe) {
        return new Tilgangsvurdering(TilgangResultat.GODKJENT, "", Set.of(kreverGruppe), null);
    }

    public static Tilgangsvurdering godkjenn(Set<AnsattGruppe> kreverGrupper) {
        return new Tilgangsvurdering(TilgangResultat.GODKJENT, "", kreverGrupper, null);
    }

    public static Tilgangsvurdering avslåGenerell(String årsak) {
        return new Tilgangsvurdering(TilgangResultat.AVSLÅTT_ANNEN_ÅRSAK, årsak, Set.of(), null);
    }
}
