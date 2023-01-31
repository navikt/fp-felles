package no.nav.vedtak.felles.integrasjon.rest;

import java.util.function.Supplier;

import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public interface RequestContextSupplier {

    Supplier<String> consumerIdFor(SikkerhetContext context);

    Supplier<OpenIDToken> tokenForSystem();

    Supplier<OpenIDToken> azureTokenForSystem(String scopes);

    Supplier<OpenIDToken> adaptive(SikkerhetContext context, String scopes);

    Supplier<OpenIDToken> consumerToken();

    boolean isAzureContext();

}
