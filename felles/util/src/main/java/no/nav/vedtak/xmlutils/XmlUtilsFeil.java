package no.nav.vedtak.xmlutils;

import org.xml.sax.SAXException;

import no.nav.vedtak.exception.TekniskException;

@Deprecated(since = "3.2.x", forRemoval = true) /* Bruk samme mapper fra felles-xmlutils */
class XmlUtilsFeil {

    private XmlUtilsFeil() {

    }

    static TekniskException fantIkkeJaxbClass(String classname, ClassNotFoundException e) {
        return new TekniskException("F-991095", String.format("Fant ikke jaxb-class '%s'", classname), e);
    }

    static TekniskException feiletVedInstansieringAvSchema(SAXException e) {
        return new TekniskException("F-350888", "Feilet på instansiering av schema for xsd-validering.", e);
    }
}
