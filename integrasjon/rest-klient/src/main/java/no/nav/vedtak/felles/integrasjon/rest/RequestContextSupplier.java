package no.nav.vedtak.felles.integrasjon.rest;

import java.util.function.Supplier;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

public interface RequestContextSupplier {

    Supplier<String> consumerIdFor(SikkerhetContext context);

    Supplier<OpenIDToken> tokenFor(SikkerhetContext context);

    Supplier<OpenIDToken> stsSystemToken();

    Supplier<OpenIDToken> azureSystemToken(String scope);

    Supplier<String> consumerId();

    Supplier<OpenIDToken> consumerToken();

}
