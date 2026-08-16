package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class JacksonRestFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(JacksonProviderFeature.class);
        context.register(JacksonContextResolver.class);
        context.register(JacksonExceptionMapper.class);

        return true;
    }
}
