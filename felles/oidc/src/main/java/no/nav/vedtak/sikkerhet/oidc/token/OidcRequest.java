package no.nav.vedtak.sikkerhet.oidc.token;

import java.net.http.HttpRequest;

import no.nav.vedtak.klient.http.DefaultRequest;

public final class OidcRequest {

    private OidcRequest() {
        // NOSONAR
    }

    public static HttpRequest.Builder builder(SikkerhetContext context) {
        return DefaultRequest.builder(() -> TokenProvider.getTokenFor(context).token(), () -> TokenProvider.getUserIdFor(context));
    }

    public static HttpRequest.Builder builderSystemSTS() {
        return DefaultRequest.builder(() -> TokenProvider.getStsSystemToken().token(), () -> TokenProvider.getUserIdFor(SikkerhetContext.SYSTEM));
    }

    public static HttpRequest.Builder builderSystemAzure(String scope) {
        return DefaultRequest.builder(() -> TokenProvider.getAzureSystemToken(scope).token(), () -> TokenProvider.getUserIdFor(SikkerhetContext.SYSTEM));
    }

    public static void consumerToken(HttpRequest.Builder builder) {
        DefaultRequest.consumerToken(builder, () -> TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token());
    }

}
