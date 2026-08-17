package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import no.nav.vedtak.server.rest.jackson.JacksonRestFeature;


public class FpRestJacksonFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(JacksonRestFeature.class);
        context.register(ValidationExceptionMapper.class);
        context.register(GeneralRestExceptionMapper.class);

        return true;
    }
}
