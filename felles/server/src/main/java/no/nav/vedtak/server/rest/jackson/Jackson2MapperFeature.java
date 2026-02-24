package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class Jackson2MapperFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(Jackson2BasicFeature.class);
        context.register(Jackson2Mapper.class);

        return true;
    }
}
