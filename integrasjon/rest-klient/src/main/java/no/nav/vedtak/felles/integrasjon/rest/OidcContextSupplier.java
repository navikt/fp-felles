package no.nav.vedtak.felles.integrasjon.rest;

import java.util.function.Supplier;

import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

public final class OidcContextSupplier {

    public Supplier<String> consumerIdForCurrentKontekst() {
        var sContext = TokenProvider.getCurrentKontekst();
        return consumerIdFor(sContext);
    }

    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> TokenProvider.getConsumerIdFor(context);
    }

    public Supplier<OpenIDToken> tokenForSystem(OpenIDProvider provider, String scopes) {
        return () -> TokenProvider.getTokenForSystem(provider, scopes);
    }

    public Supplier<OpenIDToken> adaptive(String scopes) {
        return () -> TokenProvider.getTokenForKontekst(scopes);
    }
}
