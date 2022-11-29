package no.nav.vedtak.sts.client;

import no.nav.vedtak.exception.TekniskException;
import org.apache.cxf.common.i18n.Exception;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

class StsFeil {

    private StsFeil() {

    }

    static TekniskException kanIkkeHenteSamlUtenOidcToken() {
        return new TekniskException("F-578932", "Kan ikke hente SAML uten OIDC");
    }

    static TekniskException klarteIkkeLageBuilder(ParserConfigurationException e) {
        return new TekniskException("F-411975", "Klarte ikke lage builder", e);
    }

    static TekniskException klarteIkkeLeseElement(SAXException e) {
        return new TekniskException("F-738504", "Fikk exception når forsøkte å lese onBehalfOf-element", e);
    }

    static TekniskException påkrevdSystemPropertyMangler(String nøkkel) {
        return new TekniskException("F-919615", String.format("Påkrevd system property '%s' mangler", nøkkel));
    }

    static TekniskException kunneIkkeSetteEndpointAddress(String location, Exception e) {
        return new TekniskException("F-440400", String.format("Failed to set endpoint adress of STSClient to %s", location), e);
    }
}
