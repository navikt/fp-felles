package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static com.nimbusds.oauth2.sdk.auth.JWTAuthentication.CLIENT_ASSERTION_TYPE;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.ws.rs.core.Form;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;

public class TokenXJerseyClient extends AbstractJerseyRestClient implements TokenXClient {

    private final TokenXAssertionGenerator assertionGenerator;
    private TokenXConfigMetadata metadata;

    public TokenXJerseyClient() {
        this(TokenXConfig.fraEnv());
    }

    public TokenXJerseyClient(TokenXConfig cfg) {
        this.metadata = metadataFra(cfg.wellKnownUrl());
        this.assertionGenerator = new TokenXAssertionGenerator(cfg, metadata);
    }

    TokenXJerseyClient(TokenXConfigMetadata metdata, TokenXAssertionGenerator assertionGenerator) {
        this.metadata = metdata;
        this.assertionGenerator = assertionGenerator;
    }

    @Override
    public String exchange(String token, TokenXAudience audience) {
        var form = new Form()
                .param("subject_token", token)
                .param("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                .param("client_assertion_type", CLIENT_ASSERTION_TYPE)
                .param("client_assertion", assertionGenerator.assertion())
                .param("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                .param("audience", audience.asAudience());

        return client
                .target(metadata.tokenEndpoint())
                .request(APPLICATION_FORM_URLENCODED_TYPE)
                .post(form(form), TokenXResponse.class).accessToken();
    }

    private TokenXConfigMetadata metadataFra(URI uri) {
        return client
                .target(uri)
                .request(APPLICATION_JSON_TYPE)
                .get(TokenXConfigMetadata.class);

    }
}
