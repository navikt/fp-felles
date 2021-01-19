package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;

import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;

import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenConfig;

public class StsAccessTokenJerseyClient extends AbstractJerseyRestClient implements AccessTokenProvider {

    private final StsAccessTokenConfig cfg;

    public StsAccessTokenJerseyClient(StsAccessTokenConfig cfg) {
        super(new StsAccessTokenJerseyClientRequestFilter(cfg.getUsername()));
        this.cfg = cfg;
    }

    @Override
    public String accessToken() {

        var form = new MultivaluedHashMap<String, String>();
        form.add(StsAccessTokenConfig.GRANT_TYPE, "client_credentials");
        form.add(StsAccessTokenConfig.SCOPE, "openid");
        return client.target(cfg.getStsURI())
                .register(basic(cfg.getUsername(), cfg.getPassword()))
                .request(APPLICATION_JSON_TYPE)
                .post(form(form), new GenericType<Map<String, String>>() {
                }).get("access_token");
    }

    public String getUsername() {
        return cfg.getUsername();
    }
}
