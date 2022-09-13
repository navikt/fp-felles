package no.nav.vedtak.felles.integrasjon.rest;

import java.util.function.Supplier;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

public final class OidcContextSupplier implements RequestContextSupplier {

    @Override
    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> TokenProvider.getUserIdFor(context);
    }

    @Override
    public Supplier<OpenIDToken> tokenFor(SikkerhetContext context) {
        return () -> TokenProvider.getTokenFor(context);
    }

    @Override
    public Supplier<OpenIDToken> stsSystemToken() {
        return TokenProvider::getStsSystemToken;
    }

    @Override
    public Supplier<OpenIDToken> azureSystemToken(String scope) {
        return () -> TokenProvider.getAzureSystemToken(scope);
    }

    @Override
    public Supplier<String> consumerId() {
        return () -> TokenProvider.getUserIdFor(SikkerhetContext.SYSTEM);
    }

    @Override
    public Supplier<OpenIDToken> consumerToken() {
        return () -> TokenProvider.getTokenFor(SikkerhetContext.SYSTEM);
    }
}
