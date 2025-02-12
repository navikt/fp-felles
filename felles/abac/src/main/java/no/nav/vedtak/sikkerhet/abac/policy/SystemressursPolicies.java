package no.nav.vedtak.sikkerhet.abac.policy;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;

/**
 * Inneholder tilgangspolicies for innkommende kall fra Systemressurs (Client Credentials)
 * - ConsumerId fra andre cluster-klasser er ikke tillatt (prod - dev)
 * - ConsumerId fra andre namespace er bare tillatt når endepunktet har AvailabilityType = ALL
 * - Tillatt ResourceType er alle utenom UTTAKSPLAN (evt bare APPLIKASJON, FAGSAK og PIP etter rydding)
 * - ActionType CUD og ResourceType FAGSAK krever at ForeldrepengerDataKeys BEHANDLING_STATUS eller FAGSAK_STATUS har verdi
 */
public class SystemressursPolicies {

    private static final Environment ENV = Environment.current();

    // Format: json array av objekt("name", "clientId");
    private static final String PRE_AUTHORIZED = Optional.ofNullable(ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name()))
        .orElseGet(() -> ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name().toLowerCase().replace('_', '.')));
    private static final Cluster RESIDENT_CLUSTER = ENV.getCluster();
    private static final String RESIDENT_NAMESPACE = ENV.namespace();
    private static final Set<ResourceType> IKKE_TILLATT_RESOURCE_TYPE = Set.of(ResourceType.UTTAKSPLAN);

    private SystemressursPolicies() {
        // Hindre instans
    }

    public static Tilgangsvurdering vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (!beskyttetRessursAttributter.getIdentType().erSystem()) {
            return Tilgangsvurdering.avslåGenerell("IdentType ikke Systemressurs");
        }
        if (!riktigClusterNamespacePreAuth(beskyttetRessursAttributter.getBrukerId(), beskyttetRessursAttributter.getAvailabilityType())) {
            return Tilgangsvurdering.avslåGenerell("Cluster/Namespace ikke tillatt");
        }
        if (IKKE_TILLATT_RESOURCE_TYPE.contains(beskyttetRessursAttributter.getResourceType())) {
            return Tilgangsvurdering.avslåGenerell("ResourceType UTTAKSPLAN ikke tillatt");
        }
        if (ActionType.UPDATE.equals(beskyttetRessursAttributter.getActionType()) &&
            skriveBeskyttet(beskyttetRessursAttributter, appRessursData)) {
            return Tilgangsvurdering.avslåGenerell("Sak/Behandling avsluttet");
        }
        // Skal ikke utføre videre kontroll (fagtilgang, populasjonstilgang) for System.
        return Tilgangsvurdering.godkjenn();
    }

    public static boolean riktigClusterNamespacePreAuth(String consumerId, AvailabilityType availabilityType) {
        if (consumerId == null || !PRE_AUTHORIZED.contains(consumerId)) {
            return false;
        }
        var splittConsumerId = consumerId.split(":");
        return erISammeClusterKlasse(splittConsumerId) && erISammeNamespace(splittConsumerId, availabilityType);
    }

    private static boolean erISammeClusterKlasse(String[] elementer) {
        return elementer.length > 0 && RESIDENT_CLUSTER.isSameClass(Cluster.of(elementer[0]));
    }

    private static boolean erISammeNamespace(String[] elementer, AvailabilityType availabilityType) {
        return AvailabilityType.ALL.equals(availabilityType) || elementer.length > 1 && RESIDENT_NAMESPACE.equals(elementer[1]);
    }

    private static boolean skriveBeskyttet(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        if (Objects.equals(ResourceType.FAGSAK, beskyttetRessursAttributter.getResourceType())) {
            var behandlingStatus = appRessursData.getResource(ForeldrepengerDataKeys.BEHANDLING_STATUS);
            var fagsakStatus = appRessursData.getResource(ForeldrepengerDataKeys.FAGSAK_STATUS);
            // Denne er utforskende. Vi trenger oppdatere behandling i tilstand IVED. Potensielt også AVSLU pga brevkvittering
            return behandlingStatus == null && fagsakStatus == null; // Må kunne oppdatere behandling med status IVED. Helst ikke AVSLU.
        } else {
            return true; // TODO vurder om System update/skriving kun skal være tillatt for FAGSAK (må fikse abakus-callback som har update og applikasjon)
        }
    }

}
