package no.nav.vedtak.felles.xml;

import org.xml.sax.SAXException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface XmlUtilsFeil extends DeklarerteFeil {

    XmlUtilsFeil FACTORY = FeilFactory.create(XmlUtilsFeil.class);

    @TekniskFeil(feilkode = "F-991094", feilmelding = "Fant ikke jaxb-class '%s'", logLevel = LogLevel.ERROR)
    Feil fantIkkeJaxbClass(String classname, ClassNotFoundException cause);

    @TekniskFeil(feilkode = "F-350887", feilmelding = "Feilet på instansiering av schema for xsd-validering.", logLevel = LogLevel.ERROR)
    Feil feiletVedInstansieringAvSchema(SAXException cause);
}
