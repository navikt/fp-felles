package no.nav.vedtak.sikkerhet.abac.policy;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipOverstyring;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

/**
 * Inneholder tilgangspolicies for innkommende kall fra InternBruker (OBO), dvs saksbehandler, veileder, mm.
 * - Tillatt ResourceType er alle utenom PIP og UTTAKSPLAN
 * - Vil ferdig-evaluere fagsakstatus/behandlingstatus for FAGSAK/UPDATE
 */
public class InternBrukerPolicies {

    private static final String VALUE_FP_AVDELING_ENHET_ADRESSEBESKYTTET = "2103";

    private InternBrukerPolicies() {
        // Hindre instans
    }

    public static Tilgangsvurdering vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!IdentType.InternBruker.equals(beskyttetRessursAttributter.getIdentType())) {
            return Tilgangsvurdering.avslåGenerell("Utviklerfeil identType er ikke InternBruker");
        }

        return switch (beskyttetRessursAttributter.getResourceType()) {
            case null -> Tilgangsvurdering.avslåGenerell("ikke angitt ressurs");
            case ResourceType.APPLIKASJON -> applikasjonPolicy(beskyttetRessursAttributter, appRessursData);
            case ResourceType.DRIFT -> driftPolicy(beskyttetRessursAttributter);
            case ResourceType.FAGSAK -> fagsakPolicy(beskyttetRessursAttributter, appRessursData);
            case ResourceType.VENTEFRIST -> ventefristPolicy(beskyttetRessursAttributter);
            case ResourceType.OPPGAVESTYRING_AVDELINGENHET -> avdelingEnhetPolicy(appRessursData);
            case ResourceType.OPPGAVESTYRING -> oppgavestyrerPolicy();
            default ->  Tilgangsvurdering.avslåGenerell("InternBruker har ikke tilgang til ressurs " + beskyttetRessursAttributter.getResourceType());
        };
    }

    private static Tilgangsvurdering fagsakPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        return switch (beskyttetRessursAttributter.getActionType()) {
            case READ -> erVeilederEllerSaksbehandler(beskyttetRessursAttributter);
            case CREATE -> erSaksbehandler(beskyttetRessursAttributter) ? Tilgangsvurdering.godkjenn() : Tilgangsvurdering.avslåGenerell("InternRessurs er ikke saksbehandler");
            case UPDATE -> fagsakUpdatePolicy(beskyttetRessursAttributter, appRessursData);
            case null, default -> Tilgangsvurdering.avslåGenerell("InternRessurs kan ikke utføre handling på fagsak");
        };
    }

    private static Tilgangsvurdering fagsakUpdatePolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!erSaksbehandler(beskyttetRessursAttributter)) {
            return Tilgangsvurdering.avslåGenerell("Internbruker er ikke saksbehandler");
        }
        var overstyring = getVerdi(ForeldrepengerDataKeys.AKSJONSPUNKT_OVERSTYRING, appRessursData).map(PipOverstyring::valueOf).orElse(null);
        var behandlingStatus = getVerdi(ForeldrepengerDataKeys.BEHANDLING_STATUS, appRessursData).map(PipBehandlingStatus::valueOf).orElse(null);
        var fagsakStatus = getVerdi(ForeldrepengerDataKeys.FAGSAK_STATUS, appRessursData).map(PipFagsakStatus::valueOf).orElse(null);
        // Beslutter fatter vedtak
        if (PipBehandlingStatus.FATTE_VEDTAK.equals(behandlingStatus) &&
            PipFagsakStatus.UNDER_BEHANDLING.equals(fagsakStatus) &&
            !PipOverstyring.OVERSTYRING.equals(overstyring)) {
            var saksbehandler = Optional.ofNullable(appRessursData.getResource(ForeldrepengerDataKeys.SAKSBEHANDLER)).map(RessursData::verdi).orElse(null);
            var beslutterLikSaksbehandler = Objects.equals(beskyttetRessursAttributter.getBrukerId(), saksbehandler);
            return beslutterLikSaksbehandler ? Tilgangsvurdering.avslåGenerell("Beslutter er lik saksbehandler") : Tilgangsvurdering.godkjenn(AnsattGruppe.BESLUTTER);
        }
        // Overstyring
        if (PipBehandlingStatus.UTREDES.equals(behandlingStatus) &&
            PipFagsakStatus.UNDER_BEHANDLING.equals(fagsakStatus) &&
            PipOverstyring.OVERSTYRING.equals(overstyring)) {
            return Tilgangsvurdering.godkjenn(AnsattGruppe.OVERSTYRER);
        }
        // Ordinær saksbehandling
        if ((PipBehandlingStatus.UTREDES.equals(behandlingStatus) || PipBehandlingStatus.OPPRETTET.equals(behandlingStatus)) &&
            (PipFagsakStatus.UNDER_BEHANDLING.equals(fagsakStatus) || PipFagsakStatus.OPPRETTET.equals(fagsakStatus)) &&
            !PipOverstyring.OVERSTYRING.equals(overstyring)) {
            return Tilgangsvurdering.godkjenn();
        }
        return Tilgangsvurdering.avslåGenerell("InternRessurs har ikke tilgang til å oppdatere fagsak");
    }

    private static Tilgangsvurdering applikasjonPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!appRessursData.getIdenter().isEmpty()) {
            return Tilgangsvurdering.avslåGenerell("Applikasjon-ressurs kan ikke ha personer / saker");
        }
        if (ActionType.READ.equals(beskyttetRessursAttributter.getActionType())) {
            return erVeilederEllerSaksbehandler(beskyttetRessursAttributter);
        } else {
            return Tilgangsvurdering.avslåGenerell("InternBruker kan ikke utføre handling for Applikasjon-ressurs");
        }
    }

    private static Tilgangsvurdering ventefristPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        if (ActionType.UPDATE.equals(beskyttetRessursAttributter.getActionType())) {
            return erVeilederEllerSaksbehandler(beskyttetRessursAttributter);
        } else {
            return Tilgangsvurdering.avslåGenerell("InternBruker kan ikke utføre handling for Ventefrist-ressurs");
        }
    }

    private static Tilgangsvurdering driftPolicy(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        if (ActionType.READ.equals(beskyttetRessursAttributter.getActionType()) || ActionType.CREATE.equals(beskyttetRessursAttributter.getActionType())) {
            return Tilgangsvurdering.godkjenn(AnsattGruppe.DRIFT);
        } else {
            return Tilgangsvurdering.avslåGenerell("InternRessurs kan ikke utføre handling på Drift-ressurs");
        }
    }

    private static Tilgangsvurdering oppgavestyrerPolicy() {
        return Tilgangsvurdering.godkjenn(AnsattGruppe.OPPGAVESTYRER);
    }

    private static Tilgangsvurdering avdelingEnhetPolicy(AppRessursData appRessursData) {
        var enhetAdresseBeskyttelse = Optional.ofNullable(appRessursData.getResource(ForeldrepengerDataKeys.AVDELING_ENHET))
            .map(RessursData::verdi)
            .filter(VALUE_FP_AVDELING_ENHET_ADRESSEBESKYTTET::equals)
            .isPresent();
        if (enhetAdresseBeskyttelse) {
            return Tilgangsvurdering.godkjenn(Set.of(AnsattGruppe.OPPGAVESTYRER, AnsattGruppe.STRENGTFORTROLIG));
        } else {
            return Tilgangsvurdering.godkjenn(AnsattGruppe.OPPGAVESTYRER);
        }
    }


    private static Tilgangsvurdering erVeilederEllerSaksbehandler(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        if (erSaksbehandler(beskyttetRessursAttributter) || beskyttetRessursAttributter.getAnsattGrupper().contains(AnsattGruppe.VEILEDER)) {
            return Tilgangsvurdering.godkjenn();
        } else {
            return Tilgangsvurdering.avslåGenerell("InternBruker ikke veileder eller saksbehandler");
        }
    }

    private static boolean erSaksbehandler(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        return beskyttetRessursAttributter.getAnsattGrupper().contains(AnsattGruppe.SAKSBEHANDLER);
    }

    private static Optional<String> getVerdi(ForeldrepengerDataKeys key, AppRessursData appRessursData) {
        return Optional.ofNullable(appRessursData.getResource(key))
            .map(RessursData::verdi);
    }

}
