package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@Deprecated
public interface OidcRestClientFeil extends DeklarerteFeil {

    public static final OidcRestClientFeil FACTORY = FeilFactory.create(OidcRestClientFeil.class);

    @TekniskFeil(feilkode = "F-891590", feilmelding = "IOException ved henting av systemets OIDC-token", logLevel = LogLevel.ERROR)
    Feil feilVedHentingAvSystemToken(IOException cause);

    @TekniskFeil(feilkode = "F-937072", feilmelding = "Klarte ikke å fremskaffe et OIDC token", logLevel = LogLevel.ERROR)
    Feil klarteIkkeSkaffeOIDCToken();

    @ManglerTilgangFeil(feilkode = "F-468815", feilmelding = "Mangler tilgang. Fikk http-kode 403 fra server [%s]", logLevel = LogLevel.ERROR)
    Feil manglerTilgang(URI endpoint);

    @IntegrasjonFeil(feilkode = "F-686912", feilmelding = "Server [%s] svarte med feilkode http-kode '%s' og response var '%s'", logLevel = LogLevel.WARN)
    Feil serverSvarteMedFeilkode(URI endpoint, int feilkode, String feilmelding);

    @TekniskFeil(feilkode = "F-432937", feilmelding = "IOException ved kommunikasjon med server [%s]", logLevel = LogLevel.WARN)
    Feil ioException(URI uri, IOException cause);

}