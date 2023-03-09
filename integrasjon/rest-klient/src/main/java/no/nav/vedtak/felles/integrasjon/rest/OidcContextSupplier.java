package no.nav.vedtak.felles.integrasjon.rest;

import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

import java.util.function.Supplier;

public final class OidcContextSupplier {

    public Supplier<String> consumerIdForCurrentKontekst() {
        var sContext = TokenProvider.getCurrentKontekst();
        return consumerIdFor(sContext);
    }

    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> TokenProvider.getConsumerIdFor(context);
    }

    public Supplier<OpenIDToken> tokenForSystem() {
        return TokenProvider::getTokenForSystem;
    }

    public Supplier<OpenIDToken> adaptive(String scopes) {
        return () -> TokenProvider.getTokenForKontekst(scopes);
    }

    public Supplier<OpenIDToken> azureTokenForSystem(String scopes) {
        return () -> TokenProvider.getTokenForSystem(OpenIDProvider.AZUREAD, scopes);
    }

    public Supplier<OpenIDToken> consumerToken() {
        return TokenProvider::getTokenForSystem;
    }

    public boolean isAzureContext() {
        return TokenProvider.isAzureContext();
    }
}
