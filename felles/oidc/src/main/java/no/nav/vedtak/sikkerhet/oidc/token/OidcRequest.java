package no.nav.vedtak.sikkerhet.oidc.token;

import java.util.function.Supplier;

public final class OidcRequest {

    private OidcRequest() {
        // NOSONAR
    }

    public static Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> TokenProvider.getUserIdFor(context);
    }

    public static Supplier<String> tokenFor(SikkerhetContext context) {
        return () -> TokenProvider.getTokenFor(context).token();
    }

    public static Supplier<String> stsSystemToken() {
        return () -> TokenProvider.getStsSystemToken().token();
    }

    public static Supplier<String> azureSystemToken(String scope) {
        return () -> TokenProvider.getAzureSystemToken(scope).token();
    }

    public static Supplier<String> consumerId() {
        return () -> TokenProvider.getUserIdFor(SikkerhetContext.SYSTEM);
    }

    public static Supplier<String> consumerToken() {
        return () -> TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token();
    }

}
