package no.nav.vedtak.felles.integrasjon.rest;

import static java.util.Objects.requireNonNull;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import java.time.Duration;

import no.nav.vedtak.felles.integrasjon.rest.tokenhenter.AzureAccessTokenKlient;
import no.nav.vedtak.util.LRUCache;

/**
 *
 * Se også AzureJerseyRestClient}
 *
 */
public class AzureADRestClient extends AbstractOidcRestClient {

    private final String scope;

    private final AzureAccessTokenKlient azureAccessTokenClient;

    private AzureADRestClient(String scope) {
        super(createHttpClient());
        this.scope = scope;
        this.azureAccessTokenClient = new AzureAccessTokenKlient();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    String getOIDCToken() {
        return azureAccessTokenClient.hentAccessToken(scope);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<scope=" + scope + ">";
    }

    public static class Builder {
        private String scope;

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public AzureADRestClient build() {
            return new AzureADRestClient(requireNonNull(scope));
        }
    }
}
