package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static com.nimbusds.oauth2.sdk.auth.JWTAuthentication.CLIENT_ASSERTION_TYPE;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.vedtak.log.util.ConfidentialMarkerFilter.CONFIDENTIAL;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.Form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;
import no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper;
import no.nav.vedtak.sikkerhet.oidc.WellKnownOpenIdConfiguration;

public class TokenXJerseyClient extends AbstractJerseyRestClient implements TokenXClient {
    private static final Logger LOG = LoggerFactory.getLogger(TokenXJerseyClient.class);

    private final TokenXAssertionGenerator assertionGenerator;
    private final URI tokenEndpoint;

    public TokenXJerseyClient() {
        this(TokenXConfig.fraEnv());
    }

    public TokenXJerseyClient(TokenXConfig cfg) {
        var config = WellKnownConfigurationHelper.getWellKnownConfig(cfg.wellKnownUrl());
        this.tokenEndpoint = Optional.ofNullable(config).map(WellKnownOpenIdConfiguration::token_endpoint).map(URI::create).orElse(null);
        this.assertionGenerator = new TokenXAssertionGenerator(cfg, tokenEndpoint);
    }

    @Override
    public String exchange(String token, TokenXAudience audience) {
        var exchangedToken = invoke(client
                .target(tokenEndpoint)
                .request(APPLICATION_FORM_URLENCODED_TYPE)
                .accept(APPLICATION_JSON_TYPE)
                .buildPost(form(lagForm(token, audience))), TokenXResponse.class).accessToken();
        LOG.info(CONFIDENTIAL, "Vekslet OK til {}", exchangedToken);
        return exchangedToken;
    }

    private Form lagForm(String token, TokenXAudience audience) {
        var form = new Form()
                .param("subject_token", token)
                .param("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                .param("client_assertion_type", CLIENT_ASSERTION_TYPE)
                .param("client_assertion", assertionGenerator.assertion())
                .param("subject_token_type", "urn:ietf:params:oauth:token-type:jwt")
                .param("audience", audience.asAudience());
        LOG.trace(CONFIDENTIAL, "Veksler  {}", form.asMap());
        return form;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [assertionGenerator=" + assertionGenerator + ", tokenEndpoint=" + tokenEndpoint + "]";
    }

    private record TokenXResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("issued_token_type") String issuedTokenType,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") String expiresIn) {
    }
}
