package no.nav.vedtak.felles.integrasjon.rest;

import static java.util.Objects.requireNonNull;
import static no.nav.vedtak.felles.integrasjon.rest.RestClientSupportProdusent.createHttpClient;

import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 *
 * Se også AzureJerseyRestClient}
 *
 */
public class AzureADRestClient extends AbstractOidcRestClient {

    private final String scope;

    private AzureADRestClient(String scope) {
        super(createHttpClient());
        this.scope = scope;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    String getOIDCToken() {
        return TokenProvider.getAzureSystemToken(scope).token();
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
