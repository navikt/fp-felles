package no.nav.vedtak.felles.integrasjon.rest;

import java.net.http.HttpRequest;

/**
 * For trials with custom RequestContextSuppliers
 */
public final class RestRequestExperimental extends RestRequest {

    private RestRequestExperimental() {
        super(HttpRequest.newBuilder(), TokenFlow.ADAPTIVE, null, new OidcContextSupplier());
    }

    public RestRequestExperimental(HttpRequest.Builder builder, TokenFlow tokenConfig, String scopes, RequestContextSupplier supplier) {
        super(builder, tokenConfig, scopes, supplier);
    }

}
