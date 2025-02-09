package no.nav.vedtak.sikkerhet.abac;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.EksternBrukerPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.InternBrukerPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.SystemressursPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.tilgang.AnsattGruppeKlient;
import no.nav.vedtak.sikkerhet.tilgang.PopulasjonKlient;

/**
 * Dette er strengt tatt PDP (eller en PDP-proxy før kall til Abac).
 * Interceptor er strengt tatt PEP (policy enforcement point)
 */
@Default
@ApplicationScoped
public class PepImpl implements Pep {

    private AbacAuditlogger abacAuditlogger;
    private PopulasjonKlient populasjonKlient;
    private AnsattGruppeKlient ansattGruppeKlient;
    private PdpRequestBuilder pdpRequestBuilder;


    PepImpl() {
        // CDI proxy
    }

    @Inject
    public PepImpl(AbacAuditlogger abacAuditlogger,
                   PopulasjonKlient populasjonKlient,
                   AnsattGruppeKlient ansattGruppeKlient,
                   PdpRequestBuilder pdpRequestBuilder) {
        this.abacAuditlogger = abacAuditlogger;
        this.populasjonKlient = populasjonKlient;
        this.ansattGruppeKlient = ansattGruppeKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;
    }

    @Override
    public AbacResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessurser = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

        var vurdering = forespørTilgang(beskyttetRessursAttributter, appRessurser);
        abacAuditlogger.loggUtfall(vurdering.tilgangResultat(), beskyttetRessursAttributter, appRessurser);
        return vurdering.tilgangResultat();
    }

    // Skal kunne kalles fra evt subklasser av PepImpl
    protected Tilgangsvurdering forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (ActionType.DUMMY.equals(beskyttetRessursAttributter.getActionType())) {
            return Tilgangsvurdering.avslåGenerell("ActionType DUMMY ikke støttet");
        }
        return switch (beskyttetRessursAttributter.getIdentType()) {
            case Systemressurs -> SystemressursPolicies.vurderTilgang(beskyttetRessursAttributter, appRessursData);
            case InternBruker -> forespørTilgangInternBruker(beskyttetRessursAttributter, appRessursData);
            case EksternBruker -> forespørTilgangEksternBruker(beskyttetRessursAttributter, appRessursData);
            default -> Tilgangsvurdering.avslåGenerell("Ukjent IdentType");
        };
    }

    private Tilgangsvurdering forespørTilgangInternBruker(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        HashSet<AnsattGruppe> kreverGrupper = new LinkedHashSet<>();
        // TODO: Vurdere om behov for lokal vurdering for InternBruker. Eneste use-case er k9tilbake som evt kan extende PepImpl.
        // Evt lokal vurdering av tilgang (utenom vanlig)
        if (pdpRequestBuilder.skalVurdereTilgangLokalt(beskyttetRessursAttributter, appRessursData)) {
            var lokalVurdering = pdpRequestBuilder.vurderTilgangLokalt(beskyttetRessursAttributter, appRessursData);
            if (!lokalVurdering.fikkTilgang() || pdpRequestBuilder.kunLokalVurdering(beskyttetRessursAttributter, appRessursData)) {
                return lokalVurdering;
            }
            kreverGrupper.addAll(lokalVurdering.kreverGrupper());
        }
        // Vurdering av fagtilgang
        var fagtilgang = InternBrukerPolicies.vurderTilgang(beskyttetRessursAttributter, appRessursData);
        if (!fagtilgang.fikkTilgang()) {
            return fagtilgang;
        }
        kreverGrupper.addAll(fagtilgang.kreverGrupper());
        // Vurdering av gruppemedlemskap
        var uavklarteGrupper = kreverGrupper.stream()
            .filter(g -> !harGruppe(beskyttetRessursAttributter, g))
            .collect(Collectors.toSet());
        if (!uavklarteGrupper.isEmpty()) {
            var harGrupperBlantPåkrevde = ansattGruppeKlient.vurderAnsattGrupper(beskyttetRessursAttributter.getBrukerOid(), uavklarteGrupper);
            if (uavklarteGrupper.size() != harGrupperBlantPåkrevde.size()) {
                var manglerGrupper = uavklarteGrupper.stream().filter(g -> !harGrupperBlantPåkrevde.contains(g)).toList();
                return Tilgangsvurdering.avslåGenerell("Mangler ansattgrupper " + manglerGrupper);
            }
        }
        // Vurdering av populasjonstilgang
        if (appRessursData.getFødselsnumre().isEmpty() && appRessursData.getAktørIdSet().isEmpty()) {
            // Ikke noe å sjekke for populasjonstilgang
            return Tilgangsvurdering.godkjenn();
        }
        var popTilgang = populasjonKlient.vurderTilgangInternBruker(beskyttetRessursAttributter.getBrukerOid(),
            appRessursData.getFødselsnumre(), appRessursData.getAktørIdSet());
        if (popTilgang == null) {
            return Tilgangsvurdering.avslåGenerell("Feil ved kontakt med tilgangskontroll");
        }
        return popTilgang;
    }

    private Tilgangsvurdering forespørTilgangEksternBruker(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (pdpRequestBuilder.skalVurdereTilgangLokalt(beskyttetRessursAttributter, appRessursData)) {
            var lokalVurdering = pdpRequestBuilder.vurderTilgangLokalt(beskyttetRessursAttributter, appRessursData);
            if (!lokalVurdering.fikkTilgang() || pdpRequestBuilder.kunLokalVurdering(beskyttetRessursAttributter, appRessursData)) {
                return lokalVurdering;
            }
        }
        var fagtilgang = EksternBrukerPolicies.vurderTilgang(beskyttetRessursAttributter, appRessursData);
        if (!fagtilgang.fikkTilgang()) {
            return fagtilgang;
        }
        // Ingen early return - skal sjekke alder på bruker. Kanskje populere attributt ved innsending.
        var popTilgang = populasjonKlient.vurderTilgangEksternBruker(beskyttetRessursAttributter.getBrukerId(),
            appRessursData.getFødselsnumre(), appRessursData.getAktørIdSet());
        if (popTilgang == null) {
            return Tilgangsvurdering.avslåGenerell("Feil ved kontakt med tilgangskontrll");
        }
        return popTilgang;
    }

    private static boolean harGruppe(BeskyttetRessursAttributter beskyttetRessursAttributter, AnsattGruppe gruppe) {
        return beskyttetRessursAttributter.getAnsattGrupper().stream().anyMatch(gruppe::equals);
    }


}
