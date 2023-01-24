package no.nav.vedtak.felles.integrasjon.rest;

import java.util.function.Supplier;

import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

public final class OidcContextSupplier implements RequestContextSupplier {

    @Override
    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> TokenProvider.getUserIdFor(context);
    }

    @Override
    public Supplier<OpenIDToken> tokenForSystem() {
        return TokenProvider::getTokenForSystem;
    }

    @Override
    public Supplier<OpenIDToken> adaptive(SikkerhetContext context, String scopes) {
        return () -> TokenProvider.getTokenFromCurrent(context, scopes);
    }

    @Override
    public Supplier<OpenIDToken> azureTokenForSystem(String scopes) {
        return () -> TokenProvider.getTokenForSystem(OpenIDProvider.AZUREAD, scopes);
    }

    @Override
    public Supplier<OpenIDToken> consumerToken() {
        return TokenProvider::getTokenForSystem;
    }

    @Override
    public boolean isAzureContext() {
        return TokenProvider.isAzureContext();
    }
}
