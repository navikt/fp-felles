package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.vedtak.sikkerhet.abac.AbacResultat.GODKJENT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

@Default
@ApplicationScoped
public class PepImpl implements Pep {
    private static final String PIP = ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;
    private static final Environment ENV = Environment.current();

    private PdpKlient pdpKlient;
    private PdpRequestBuilder builder;

    private String preAuthorized;
    private Cluster residentCluster;
    private String residentNamespace;

    PepImpl() {
        // CDI proxy
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient, PdpRequestBuilder pdpRequestBuilder) {
        this.pdpKlient = pdpKlient;
        this.builder = pdpRequestBuilder;
        this.preAuthorized = ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name()); // eg json array av objekt("name", "clientId")
        this.residentCluster = ENV.getCluster();
        this.residentNamespace = ENV.namespace();
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessurser = builder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

        if (kanForetaLokalTilgangsbeslutning(beskyttetRessursAttributter.getToken())) {
            return vurderLokalTilgang(beskyttetRessursAttributter, appRessurser);
        } else if (PIP.equals(beskyttetRessursAttributter.getResourceType())) { // pip tilgang bør vurderes kun lokalt
            return new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessurser);
        }

        return pdpKlient.forespørTilgang(beskyttetRessursAttributter, builder.abacDomene(), appRessurser);
    }

    protected Tilgangsbeslutning vurderLokalTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        var token = beskyttetRessursAttributter.getToken();
        var harTilgang = harTilgang(token.getBrukerId(), beskyttetRessursAttributter.getAvailabilityType());
        return new Tilgangsbeslutning(harTilgang ? GODKJENT : AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessursData);
    }

    // AzureAD CC kommer med sub som ikke ikke en bruker med vanlige AD-grupper og roller
    // Token kan utvides med roles og groups - men oppsettet er langt fra det som er kjent fra STS mv.
    // Kan legge inn filter på claims/roles intern og/eller ekstern.
    private boolean kanForetaLokalTilgangsbeslutning(Token token) {
        var identType = token.getIdentType();
        var consumer = token.getBrukerId();

        return OpenIDProvider.AZUREAD.equals(token.getOpenIDProvider()) &&
            IdentType.Systemressurs.equals(identType) &&
            consumer != null &&
            preAuthorized != null;
    }

    private boolean harTilgang(String consumerId, AvailabilityType availabilityType) {
        if (consumerId == null || !preAuthorized.contains(consumerId)) {
            return false;
        }

        if (erISammeKlusterKlasseOgNamespace(consumerId)) {
            return true;
        }

        return AvailabilityType.ALL.equals(availabilityType);
    }

    private boolean erISammeKlusterKlasseOgNamespace(String consumer) {
        var elementer = consumer.split(":");
        if (elementer.length < 2) {
            return false;
        }

        var consumerCluster = elementer[0];
        var consumerNamespace = elementer[1];
        return residentCluster.isSameClass(Cluster.of(consumerCluster)) && residentNamespace.equals(consumerNamespace);
    }

}
