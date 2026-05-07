package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class Jackson3RestFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(Jackson3ProviderFeature.class);
        context.register(Jackson3ContextResolver.class);
        context.register(Jackson3ExceptionMapper.class);

        return true;
    }
}
