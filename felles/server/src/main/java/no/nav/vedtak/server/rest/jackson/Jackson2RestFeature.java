package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class Jackson2RestFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(Jackson2ProviderFeature.class);
        context.register(Jackson2ContextResolver.class);
        context.register(Jackson2ExceptionMapper.class);

        return true;
    }
}
