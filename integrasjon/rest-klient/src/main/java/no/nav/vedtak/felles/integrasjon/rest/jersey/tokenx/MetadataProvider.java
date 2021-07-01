package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;

import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

interface MetadataProvider {

    default AuthorizationServerMetadata retrieve(String uri) {
        return retrieve(URI.create(uri));
    }

    AuthorizationServerMetadata retrieve(URI uri);

}
