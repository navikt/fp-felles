package no.nav.vedtak.sikkerhet.abac.policy;

import java.util.Objects;
import java.util.Optional;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursData;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

/**
 * Inneholder tilgangspolicies for innkommende kall fra EksternBruker (Client Credentials)
 * - Tillatt ResourceType er APPLIKASJON, FAGSAK og etterhver UTTAKSPLAN
 * - Krav til innlogging nivå 4 er håndtert i autentisering
 * - Alderssjekk 15/18 år må gjøres i populasjonstilgang
 * - Enn så lenge delegeres øvrige sjekker til populasjonstilgang - gjelder egen person + krav rundt uttakspln
 * - Kan forenkles dersom man legger om til å populere med fnr i stedet for aktørId - må fortsatt kalle videre for alderssjekk
 */
public class EksternBrukerPolicies {

    private EksternBrukerPolicies() {
        // Hindre instans
    }

    public static Tilgangsvurdering vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!IdentType.EksternBruker.equals(beskyttetRessursAttributter.getIdentType())) {
            return Tilgangsvurdering.avslåGenerell("Utviklerfeil identType er ikke EksternBruker");
        }

        return switch (beskyttetRessursAttributter.getResourceType()) {
            case null -> Tilgangsvurdering.avslåGenerell("ikke angitt ressurs");
            case ForeldrepengerAttributter.RESOURCE_TYPE_FP_APPLIKASJON -> applikasjonPolicy(beskyttetRessursAttributter, appRessursData);
            case ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK -> fagsakPolicy(beskyttetRessursAttributter, appRessursData);
            case ForeldrepengerAttributter.RESOURCE_TYPE_FP_UTTAKSPLAN -> uttaksplanPolicy(beskyttetRessursAttributter, appRessursData);
            default ->  Tilgangsvurdering.avslåGenerell("EksternBruker har ikke tilgang til ressurs");
        };
    }

    private static Tilgangsvurdering fagsakPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (ActionType.READ.equals(beskyttetRessursAttributter.getActionType()) || ActionType.CREATE.equals(beskyttetRessursAttributter.getActionType())) {
            // Vurder å sjekke for appressursdato.fnr() - men må uansett videre til alderssjekk
            return Tilgangsvurdering.godkjenn();
        } else {
            return Tilgangsvurdering.avslåGenerell("EksternBruker kan ikke utføre handling på fagsak");
        }
    }

    private static Tilgangsvurdering applikasjonPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!appRessursData.getFødselsnumre().isEmpty() || !appRessursData.getAktørIdSet().isEmpty()) {
            return Tilgangsvurdering.avslåGenerell("Applikasjon-ressurs kan ikke ha personer / saker");
        }
        if (ActionType.READ.equals(beskyttetRessursAttributter.getActionType())) {
            return Tilgangsvurdering.godkjenn();
        } else {
            return Tilgangsvurdering.avslåGenerell("EksternBruker kan ikke utføre handling for ressurs");
        }
    }

    private static Tilgangsvurdering uttaksplanPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        var annenpart = Optional.ofNullable(appRessursData.getResource(ForeldrepengerDataKeys.ANNENPART)).map(RessursData::verdi).orElse(null);
        var aleneomsorg = Optional.ofNullable(appRessursData.getResource(ForeldrepengerDataKeys.ALENEOMSORG)).map(RessursData::verdi).orElse(null);
        if (ActionType.READ.equals(beskyttetRessursAttributter.getActionType())) {
            // Tester for om gjelderEgenPerson eller brukerId = annenpart_fnr
            return Tilgangsvurdering.avslåGenerell("Ikke implementert"); // TODO fortsett med mindre Deny
        } else {
            return Tilgangsvurdering.avslåGenerell("EksternBruker kan ikke utføre handling for ressurs");
        }
    }

    private static boolean gjelderEgenPersonFnr(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        var fnr = appRessursData.getFødselsnumre();
        return fnr.size() == 1 && Objects.equals(beskyttetRessursAttributter.getBrukerId(), fnr.stream().findFirst().orElse(null));
    }

}
