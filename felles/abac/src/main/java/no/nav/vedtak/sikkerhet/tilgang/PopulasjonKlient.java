package no.nav.vedtak.sikkerhet.tilgang;

import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;

public interface PopulasjonKlient {

    Tilgangsvurdering vurderTilgang(PopulasjonInternRequest request);
    Tilgangsvurdering vurderTilgang(PopulasjonEksternRequest request);

}
