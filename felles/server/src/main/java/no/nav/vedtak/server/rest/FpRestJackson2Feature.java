package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import no.nav.vedtak.server.rest.jackson.Jackson2MapperFeature;


public class FpRestJackson2Feature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(AuthenticationFilter.class);
        context.register(Jackson2MapperFeature.class);
        context.register(ValidationExceptionMapper.class);
        context.register(GeneralRestExceptionMapper.class);

        return true;
    }
}
