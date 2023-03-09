package no.nav.foreldrepenger.xmlutils;

import no.nav.vedtak.exception.TekniskException;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class XmlUtils {

    private static final String TARGET_NAMESPACE = "targetNamespace";

    private XmlUtils() {
    }

    public static Map<String, Map.Entry<Class<?>, Schema>> createUnmodifiableMap(String jaxbClassName, List<String> namespaces,
                                                                                 List<String> xsdLocations) {
        if (namespaces.size() != xsdLocations.size()) {
            throw new IllegalArgumentException();
        }
        Map<String, Map.Entry<Class<?>, Schema>> tempMap;
        try {
            final Class<?> jaxbClass = Class.forName(jaxbClassName);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            tempMap = new HashMap<>();
            for (int i = 0; i < namespaces.size(); i++) {
                var schema = schemaFactory
                    .newSchema(new StreamSource(XmlUtils.class.getClassLoader().getResource(xsdLocations.get(i)).toExternalForm()));
                tempMap.put(namespaces.get(i), new SimpleEntry<>(jaxbClass, schema));
            }
        } catch (SAXException e) {
            throw new TekniskException("F-350888", "Feilet på instansiering av schema for xsd-validering.", e);
        } catch (ClassNotFoundException e) {
            throw new TekniskException("F-991095", String.format("Fant ikke jaxb-class '%s'", jaxbClassName), e);
        }
        return Collections.unmodifiableMap(tempMap);
    }

    public static Map<String, Map.Entry<Class<?>, Schema>> createUnmodifiableMap(String jaxbClassName, String namespace, String xsdLocation) {

        Map<String, Map.Entry<Class<?>, Schema>> tempMap;
        try {
            final Class<?> jaxbClass = Class.forName(jaxbClassName);

            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory
                .newSchema(new StreamSource(XmlUtils.class.getClassLoader().getResource(xsdLocation).toExternalForm()));
            tempMap = Collections.singletonMap(namespace, new SimpleEntry<>(jaxbClass, schema));
        } catch (SAXException e) {
            throw new TekniskException("F-350888", "Feilet på instansiering av schema for xsd-validering.", e);
        } catch (ClassNotFoundException e) {
            throw new TekniskException("F-991095", String.format("Fant ikke jaxb-class '%s'", jaxbClassName), e);
        }
        return Collections.unmodifiableMap(tempMap);
    }

    public static String retrieveNameSpaceOfXML(Source xmlSource) throws XMLStreamException {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlif.createXMLStreamReader(xmlSource);
        while (!xmlStreamReader.isStartElement()) {
            xmlStreamReader.next();
        }
        return xmlStreamReader.getNamespaceURI();
    }

    public static String retrieveNameSpaceOfXML(String xml) throws XMLStreamException {
        try (final StringReader reader = new StringReader(xml)) {
            return retrieveNameSpaceOfXML(new StreamSource(reader));
        }
    }

    public static String retrieveNameSpaceOfXSD(String xsd) throws XMLStreamException {
        try (final StringReader reader = new StringReader(xsd)) {
            return retrieveNameSpaceOfXML(new StreamSource(reader));
        }
    }

    public static String retrieveNameSpaceOfXSD(Source xsdSource) throws XMLStreamException {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlif.createXMLStreamReader(xsdSource);
        while (!xmlStreamReader.isStartElement()) {
            xmlStreamReader.next();
        }
        return xmlStreamReader.getAttributeValue(null, TARGET_NAMESPACE);
    }
}
