package no.nav.vedtak.sikkerhet.oidc.token;

import java.util.Optional;
import java.util.Set;

import no.nav.vedtak.sikkerhet.kontekst.BasisKontekst;
import no.nav.vedtak.sikkerhet.kontekst.DefaultRequestKontekstProvider;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstProvider;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureBrukerTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureSystemTokenKlient;
import no.nav.vedtak.sikkerhet.oidc.token.impl.TokenXExchangeKlient;

public final class TokenProvider {

    private static final KontekstProvider KONTEKST_PROVIDER = new DefaultRequestKontekstProvider();
    private static final Set<SikkerhetContext> USE_SYSTEM = Set.of(SikkerhetContext.SYSTEM);

    private TokenProvider() {
    }

    public static SikkerhetContext getCurrentKontekst() {
        return KONTEKST_PROVIDER.getKontekst().getContext();
    }

    public static OpenIDToken getTokenForKontekst(String scopes) {
        var kontekst = KONTEKST_PROVIDER.getKontekst();
        if (USE_SYSTEM.contains(kontekst.getContext())) {
            return getAzureSystemToken(scopes);
        }
        if (kontekst instanceof RequestKontekst requestKontekst) {
            return getOutgoingTokenFor(requestKontekst, scopes);
        } else {
            throw new IllegalStateException("Mangler SikkerhetContext - skal ikke provide token");
        }
    }

    private static OpenIDToken getOutgoingTokenFor(RequestKontekst requestKontekst, String scopes) {
        var incoming = requestKontekst.getToken();
        if (incoming == null || incoming.token() == null) {
            return incoming;
        }
        var providerIncoming = getProvider(incoming);
        var identType = Optional.ofNullable(requestKontekst.getIdentType()).orElse(IdentType.InternBruker);
        return switch (providerIncoming) {
            case AZUREAD -> identType.erSystem() ? getAzureSystemToken(scopes) : veksleAzureAccessToken(requestKontekst.getUid(), incoming, scopes);
            case TOKENX -> tokenXchange(incoming, scopes);
        };
    }

    public static OpenIDToken getTokenForSystem(String scopes) {
        return getAzureSystemToken(scopes);
    }

    // Endre til AzureClientId ved overgang til system = azure
    public static String getConsumerIdFor(SikkerhetContext context) {
        return switch (context) {
            case REQUEST -> getCurrentConsumerId();
            case SYSTEM -> BasisKontekst.getAppConsumerId();
        };
    }

    public static String getCurrentConsumerId() {
        var kontekst = KONTEKST_PROVIDER.getKontekst();
        return Optional.ofNullable(kontekst.getKonsumentId()).orElseGet(kontekst::getUid);
    }

    private static OpenIDToken getAzureSystemToken(String scopes) {
        return AzureSystemTokenKlient.instance().hentAccessToken(scopes);
    }

    private static OpenIDToken veksleAzureAccessToken(String uid, OpenIDToken incoming, String scopes) {
        return AzureBrukerTokenKlient.instance().oboExchangeToken(uid, incoming, scopes);
    }

    public static OpenIDToken tokenXchange(OpenIDToken token, String scopes) {
        return TokenXExchangeKlient.instance().exchangeToken(token, scopes);
    }

    private static OpenIDProvider getProvider(OpenIDToken token) {
        return Optional.ofNullable(token).map(OpenIDToken::provider).orElse(null);
    }

}
