package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.io.IOException;
import java.net.URI;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

import no.nav.vedtak.exception.TekniskException;

class DefaultMetadataProvider implements MetadataProvider {

    private final ResourceRetriever retriever;

    public DefaultMetadataProvider() {
        this(new DefaultResourceRetriever());
    }

    public DefaultMetadataProvider(ResourceRetriever retriever) {
        this.retriever = retriever;
    }

    @Override
    public AuthorizationServerMetadata retrieve(String uri) {
        return retrieve(URI.create(uri));
    }

    @Override
    public AuthorizationServerMetadata retrieve(URI uri) {
        try {
            return AuthorizationServerMetadata.parse(retriever.retrieveResource(uri.toURL()).getContent());
        } catch (ParseException | IOException e) {
            throw new TekniskException("F-999999", String.format("exception when retrieving metadata from endpoint %s", uri), e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [retriever=" + retriever + "]";
    }
}
