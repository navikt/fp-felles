package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.vedtak.sikkerhet.abac.AbacResultat.GODKJENT;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.EksternBrukerPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.abac.policy.InternBrukerPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.SystemressursPolicies;
import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonEksternRequest;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonInternRequest;
import no.nav.vedtak.sikkerhet.populasjon.PopulasjonKlient;

/**
 * Dette er strengt tatt PDP (eller en PDP-proxy før kall til Abac).
 * Interceptor er strengt tatt PEP (policy enforcement point)
 */
@Default
@ApplicationScoped
public class PepImpl implements Pep {

    private static final Logger LOG = LoggerFactory.getLogger(PepImpl.class);
    private static final String PIP = ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;

    private PdpKlient pdpKlient;
    private PopulasjonKlient populasjonKlient;
    private PdpRequestBuilder pdpRequestBuilder;


    PepImpl() {
        // CDI proxy
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient, PopulasjonKlient populasjonKlient, PdpRequestBuilder pdpRequestBuilder) {
        this.pdpKlient = pdpKlient;
        this.populasjonKlient = populasjonKlient;
        this.pdpRequestBuilder = pdpRequestBuilder;
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessurser = pdpRequestBuilder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

        if (IdentType.Systemressurs.equals(beskyttetRessursAttributter.getIdentType())) {
            return vurderLokalTilgang(beskyttetRessursAttributter, appRessurser);
        } else if (PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
            return new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessurser);
        }

        var pdpResultat = pdpKlient.forespørTilgang(beskyttetRessursAttributter, pdpRequestBuilder.abacDomene(), appRessurser);
        sammenlignOgLogg(beskyttetRessursAttributter, appRessurser, pdpResultat.beslutningKode());
        return pdpResultat;
    }

    protected Tilgangsbeslutning vurderLokalTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        var harTilgang = SystemressursPolicies.riktigClusterNamespacePreAuth(beskyttetRessursAttributter.getBrukerId(), beskyttetRessursAttributter.getAvailabilityType());
        return new Tilgangsbeslutning(harTilgang ? GODKJENT : AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessursData);
    }

    private void sammenlignOgLogg(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData, AbacResultat resultat) {
        try {
            var lokalt = forespørTilgang(beskyttetRessursAttributter, appRessursData);
            if (Objects.equals(lokalt.tilgangResultat(), resultat)) {
                LOG.info("FPEGENTILGANG: samme svar");
            } else {
                var metode = beskyttetRessursAttributter.getServicePath();
                LOG.info("FPEGENTILGANG: ulikt svar - gammel {} ny {} - metode {}", resultat, lokalt.tilgangResultat(), metode);
            }
        } catch (Exception e) {
            LOG.info("FPEGENTILGANG: feil", e);
        }
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
        if (pdpRequestBuilder.skalVurdereTilgangLokalt(beskyttetRessursAttributter, appRessursData)) {
            var lokalVurdering = pdpRequestBuilder.vurderTilgangLokalt(beskyttetRessursAttributter, appRessursData);
            if (!lokalVurdering.fikkTilgang() || pdpRequestBuilder.kunLokalVurdering(beskyttetRessursAttributter, appRessursData)) {
                return lokalVurdering;
            }
            kreverGrupper.addAll(lokalVurdering.kreverGrupper());
        }
        var fagtilgang = InternBrukerPolicies.vurderTilgang(beskyttetRessursAttributter, appRessursData);
        if (!fagtilgang.fikkTilgang()) {
            return fagtilgang;
        }
        kreverGrupper.addAll(fagtilgang.kreverGrupper());
        if (kreverGrupper.isEmpty() && appRessursData.getFødselsnumre().isEmpty() && appRessursData.getAktørIdSet().isEmpty()) {
            // Ikke noe å sjekke for populasjonstilgang
            return Tilgangsvurdering.godkjenn();
        }
        var popRequest = new PopulasjonInternRequest(beskyttetRessursAttributter.getBrukerOid(), kreverGrupper,
            appRessursData.getFødselsnumre(), appRessursData.getAktørIdSet());
        var popTilgang = populasjonKlient.vurderTilgang(popRequest);
        if (popTilgang == null) {
            return Tilgangsvurdering.avslåGenerell("Feil ved kontakt med tilgangskontrll");
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
        var popRequest = new PopulasjonEksternRequest(beskyttetRessursAttributter.getBrukerId(),
            appRessursData.getFødselsnumre(), appRessursData.getAktørIdSet());
        var popTilgang = populasjonKlient.vurderTilgang(popRequest);
        if (popTilgang == null) {
            return Tilgangsvurdering.avslåGenerell("Feil ved kontakt med tilgangskontrll");
        }
        return popTilgang;
    }


}
