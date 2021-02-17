package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;

@Deprecated
class OidcRestClientFeil {

    private OidcRestClientFeil() {
    }

    static TekniskException feilVedHentingAvSystemToken(IOException e) {
        return new TekniskException("F-891590", "IOException ved henting av systemets OIDC-token", e);
    }

    static TekniskException klarteIkkeSkaffeOIDCToken() {
        return new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token");
    }

    static ManglerTilgangException manglerTilgang(URI endpoint) {
        return new ManglerTilgangException("F-468815", String.format("Mangler tilgang. Fikk http-kode 403 fra server [%s]", endpoint));
    }

    static IntegrasjonException serverSvarteMedFeilkode(URI endpoint, int feilkode, String feilmelding) {
        return new IntegrasjonException("F-686912",
                String.format("Server [%s] svarte med feilkode http-kode '%s' og response var '%s'", endpoint, feilkode, feilmelding));
    }

    static TekniskException ioException(URI endpoint, IOException e) {
        return new TekniskException("F-432937", String.format("IOException ved kommunikasjon med server [%s]", endpoint), e);
    }
}