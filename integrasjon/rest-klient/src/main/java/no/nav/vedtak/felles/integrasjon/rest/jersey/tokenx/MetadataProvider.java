package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;

import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

interface MetadataProvider {

    AuthorizationServerMetadata retrieve(String uri);

    AuthorizationServerMetadata retrieve(URI uri);

}
