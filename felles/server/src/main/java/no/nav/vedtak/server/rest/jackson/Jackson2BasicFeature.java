package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;


public class Jackson2BasicFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        // Register Jackson.
        if (!config.isRegistered(JacksonJsonProvider.class)) {
            context.register(JacksonJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        }
        context.register(JsonMappingExceptionMapper.class);
        context.register(JsonParseExceptionMapper.class);

        return true;
    }
}
