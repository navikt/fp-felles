package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;

/**
 * Dette grensesnittet må implementeres av alle applikasjoner:
 * - Alle må implementere lagAppRessursData
 * - De applikasjonene (tilbake, inntektsmelding) som skal kalle K9 sin PDP/abac må implementere abacDomene
 * -
 */
public interface PdpRequestBuilder {

    default String abacDomene() {
        return "foreldrepenger";
    }

    AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter);

    // Trenger egentlig bare sette BEHANDLING_STATUS + FAGSAK_STATUS i tilfelle FAGSAK / UPDATE for skrivetilgangs-sjekk
    default AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        return lagAppRessursData(dataAttributter);
    }


    default boolean skalVurdereTilgangLokalt(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        return false;
    }

    // Fortsette med vanlig tilgangskontroll eller bruke svar fra lokal vurdering
    default boolean kunLokalVurdering(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        return false;
    }

    default Tilgangsvurdering vurderTilgangLokalt(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        throw new IllegalArgumentException("Utviklerfeil: viser lokal tilgangsvurdering, men ikke implementert metode");
    }
}
