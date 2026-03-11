package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class Jackson3MapperFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(Jackson3BasicFeature.class);
        context.register(Jackson3Mapper.class);

        return true;
    }
}
