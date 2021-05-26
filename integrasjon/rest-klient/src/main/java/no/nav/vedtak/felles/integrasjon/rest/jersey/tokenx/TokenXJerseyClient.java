package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static com.nimbusds.oauth2.sdk.auth.JWTAuthentication.CLIENT_ASSERTION_TYPE;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.util.env.ConfidentialMarkerFilter.CONFIDENTIAL;

import javax.ws.rs.core.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;

public class TokenXJerseyClient extends AbstractJerseyRestClient implements TokenXClient {
    private static final Logger LOG = LoggerFactory.getLogger(TokenXJerseyClient.class);

    private final TokenXAssertionGenerator assertionGenerator;
    private final AuthorizationServerMetadata metadata;

    public TokenXJerseyClient() {
        this(TokenXConfig.fraEnv(), new DefaultMetadataProvider());
    }

    public TokenXJerseyClient(TokenXConfig cfg, MetadataProvider metdataProvider) {
        this.metadata = metdataProvider.retrieve(cfg.wellKnownUrl());
        this.assertionGenerator = new TokenXAssertionGenerator(cfg, metadata.getTokenEndpointURI());
    }

    TokenXJerseyClient(AuthorizationServerMetadata metdata, TokenXAssertionGenerator assertionGenerator) {
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
        LOG.trace(CONFIDENTIAL, "Veksler  {}", form.asMap());
        var exchangedToken = client
                .target(metadata.getTokenEndpointURI())
                .request(APPLICATION_FORM_URLENCODED_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .post(form(form), TokenXResponse.class).accessToken();
        LOG.trace(CONFIDENTIAL, "Vekslet OK til {}", exchangedToken);
        return exchangedToken;

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [assertionGenerator=" + assertionGenerator + ", metadata=" + metadata + "]";
    }
}
