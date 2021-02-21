package no.nav.vedtak.felles.xml;

import org.xml.sax.SAXException;

import no.nav.vedtak.exception.TekniskException;

/**
 * @deprecated bruk samme klasse fra felles-util
 */
@Deprecated(since = "3.0.x", forRemoval = true)
class XmlUtilsFeil {

    private XmlUtilsFeil() {

    }

    static TekniskException fantIkkeJaxbClass(String classname, ClassNotFoundException e) {
        return new TekniskException("F-991094", String.format("Fant ikke jaxb-class '%s'", classname), e);
    }

    static TekniskException feiletVedInstansieringAvSchema(SAXException e) {
        return new TekniskException("F-350887", "Feilet på instansiering av schema for xsd-validering.", e);
    }
}
