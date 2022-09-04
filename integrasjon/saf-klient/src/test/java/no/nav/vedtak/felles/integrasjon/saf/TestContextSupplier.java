package no.nav.vedtak.felles.integrasjon.saf;

import java.util.function.Supplier;

import no.nav.vedtak.felles.integrasjon.rest.RequestContextSupplier;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public final class TestContextSupplier implements RequestContextSupplier {

    private static final String DUMMY_TOKEN = "TOKEN";

    public TestContextSupplier() {
    }

    @Override
    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> "user";
    }

    @Override
    public Supplier<OpenIDToken> tokenFor(SikkerhetContext context) {
        return () -> new OpenIDToken(OpenIDProvider.TOKENX, new TokenString(DUMMY_TOKEN));
    }

    @Override
    public Supplier<OpenIDToken> stsSystemToken() {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN));
    }

    @Override
    public Supplier<OpenIDToken> azureSystemToken(String scope) {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN));
    }

    @Override
    public Supplier<String> consumerId() {
        return () -> "system";
    }

    @Override
    public Supplier<OpenIDToken> consumerToken() {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN));
    }
}
