package no.nav.vedtak.sts.client;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import org.apache.cxf.common.i18n.Exception;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

interface StsFeil extends DeklarerteFeil {
    StsFeil FACTORY = FeilFactory.create(StsFeil.class);

    @TekniskFeil(feilkode = "F-578932", feilmelding = "Kan ikke hente SAML uten OIDC", logLevel = LogLevel.WARN)
    Feil kanIkkeHenteSamlUtenOidcToken();

    @TekniskFeil(feilkode = "F-411975", feilmelding = "Klarte ikke lage builder", logLevel = LogLevel.ERROR)
    Feil klarteIkkeLageBuilder(ParserConfigurationException e);

    @TekniskFeil(feilkode = "F-738504", feilmelding = "Fikk exception når forsøkte å lese onBehalfOf-element", logLevel = LogLevel.WARN)
    Feil klarteIkkeLeseElement(SAXException e);

    @TekniskFeil(feilkode = "F-919615", feilmelding = "Påkrevd system property '%s' mangler", logLevel = LogLevel.ERROR)
    Feil påkrevdSystemPropertyMangler(String nøkkel);

    @TekniskFeil(feilkode = "F-440400", feilmelding = "Failed to set endpoint adress of STSClient to %s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeSetteEndpointAddress(String location, Exception cause);
}
