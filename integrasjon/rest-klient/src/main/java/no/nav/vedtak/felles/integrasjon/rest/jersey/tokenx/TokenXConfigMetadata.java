package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.net.URI;

import com.nimbusds.oauth2.sdk.id.Issuer;

record TokenXConfigMetadata(Issuer issuer, URI tokenEndpoint) {
}
