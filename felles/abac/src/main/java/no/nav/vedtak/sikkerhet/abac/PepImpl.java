package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
import static no.nav.vedtak.sikkerhet.abac.AbacResultat.GODKJENT;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.context.containers.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

@Default
@ApplicationScoped
public class PepImpl implements Pep {
    private static final String PIP = ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;

    private PdpKlient pdpKlient;
    private PdpRequestBuilder builder;

    private Set<String> pipUsers;
    private TokenProvider tokenProvider;
    private AbacAuditlogger auditlogger;

    public PepImpl() {
    }

    @Inject
    public PepImpl(PdpKlient pdpKlient,
            TokenProvider tokenProvider,
            PdpRequestBuilder pdpRequestBuilder,
            AbacAuditlogger auditlogger,
            @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        this.pdpKlient = pdpKlient;
        this.builder = pdpRequestBuilder;
        this.tokenProvider = tokenProvider;
        this.auditlogger = auditlogger;
        this.pipUsers = konfigurePipUsers(pipUsers);
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
        if (lokalTilgangsbeslutning(beskyttetRessursAttributter)) {
            return new Tilgangsbeslutning(GODKJENT, beskyttetRessursAttributter, appRessurser);
        }
        return pdpKlient.forespørTilgang(beskyttetRessursAttributter, builder.abacDomene(), appRessurser);
    }

    // AzureAD CC kommer med sub som ikke ikke en bruker med vanlige AD-grupper og roller
    // Token kan utvides med roles og groups - men oppsettet er langt fra det som er kjent fra STS mv.
    // Kan legge inn filter på claims/roles intern og/eller ekstern.
    private boolean lokalTilgangsbeslutning(BeskyttetRessursAttributter attributter) {
        return builder.kanBeslutteSystemtilgangLokalt(attributter.getActionType(), attributter.getResourceType(), attributter.getServicePath()) &&
            IdentType.Systemressurs.equals(attributter.getToken().getIdentType()) &&
            OpenIDProvider.AZUREAD.equals(attributter.getToken().getOpenIDProvider());
    }

    protected Tilgangsbeslutning vurderTilgangTilPipTjeneste(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        String uid = tokenProvider.getUid();
        if (pipUsers.contains(uid.toLowerCase())) {
            return new Tilgangsbeslutning(GODKJENT, beskyttetRessursAttributter, appRessursData);
        }
        var tilgangsbeslutning = new Tilgangsbeslutning(AVSLÅTT_ANNEN_ÅRSAK, beskyttetRessursAttributter, appRessursData);
        auditlogger.loggDeny(uid, tilgangsbeslutning);
        return tilgangsbeslutning;
    }

}
