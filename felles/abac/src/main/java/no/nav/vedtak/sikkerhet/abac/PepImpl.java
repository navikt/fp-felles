package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.vedtak.sikkerhet.abac.AbacResultat.GODKJENT;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
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

    private Set<String> pipUsers;
    private TokenProvider tokenProvider;
    private String preAuthorized;
    private String residentClusterNamespace;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient,
                   TokenProvider tokenProvider,
                   PdpRequestBuilder pdpRequestBuilder,
                   @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdpKlient = pdpKlient;
        this.builder = pdpRequestBuilder;
        this.tokenProvider = tokenProvider;
        this.pipUsers = konfigurePipUsers(pipUsers);
        this.preAuthorized = ENV.getProperty(AzureProperty.AZURE_APP_PRE_AUTHORIZED_APPS.name()); // eg json array av objekt("name", "clientId")
        this.residentClusterNamespace = ENV.clusterName() + ":" + ENV.namespace();
    }

    protected Set<String> konfigurePipUsers(String pipUsers) {
        if (pipUsers != null) {
            return Set.of(pipUsers.toLowerCase().split(","));
        }
        return Set.of();
    }

    @Override
    public Tilgangsbeslutning vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        var appRessurser = builder.lagAppRessursData(beskyttetRessursAttributter.getDataAttributter());

        if (PIP.equals(beskyttetRessursAttributter.getResourceType())) {
            return vurderTilgangTilPipTjeneste(beskyttetRessursAttributter, appRessurser);
        }
        if (kanForetaLokalTilgangsbeslutning(beskyttetRessursAttributter)) {
            return new Tilgangsbeslutning(harTilgang(beskyttetRessursAttributter) ? GODKJENT : AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessurser);
        }
        return pdpKlient.forespørTilgang(beskyttetRessursAttributter, builder.abacDomene(), appRessurser);
    }

    // AzureAD CC kommer med sub som ikke ikke en bruker med vanlige AD-grupper og roller
    // Token kan utvides med roles og groups - men oppsettet er langt fra det som er kjent fra STS mv.
    // Kan legge inn filter på claims/roles intern og/eller ekstern.
    private boolean kanForetaLokalTilgangsbeslutning(BeskyttetRessursAttributter attributter) {
        var identType = attributter.getToken().getIdentType();
        var consumer = attributter.getToken().getBrukerId();
        return OpenIDProvider.AZUREAD.equals(attributter.getToken().getOpenIDProvider())
            && IdentType.Systemressurs.equals(identType) && consumer != null && preAuthorized != null;
    }

    private boolean harTilgang(BeskyttetRessursAttributter attributter) {
        var consumer = attributter.getToken().getBrukerId();
        if (consumer == null || !preAuthorized.contains(consumer)) {
            return false;
        }
        if (consumer.startsWith(residentClusterNamespace) || builder.internAzureConsumer(consumer)) {
            return true;
        }
        return AvailabilityType.ALL.equals(attributter.getAvailabilityType());
    }

    protected Tilgangsbeslutning vurderTilgangTilPipTjeneste(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        String uid = tokenProvider.getUid();
        if (pipUsers.contains(uid.toLowerCase())) {
            return new Tilgangsbeslutning(GODKJENT, beskyttetRessursAttributter, appRessursData);
        } else if (kanForetaLokalTilgangsbeslutning(beskyttetRessursAttributter) && harTilgang(beskyttetRessursAttributter)) {
            return new Tilgangsbeslutning(GODKJENT, beskyttetRessursAttributter, appRessursData);
        } else {
            return new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessursData);
        }
    }

}
