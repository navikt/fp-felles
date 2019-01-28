package no.nav.vedtak.sikkerhet.loginmodule;

import no.nav.vedtak.log.util.LoggerUtils;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * A collection of utilities used to deserialize SAML token.
 */
public class SamlUtils {
    private static final String IDENT_TYPE = "identType";
    private static final String AUTHENTICATION_LEVEL = "authenticationLevel";
    private static final String CONSUMER_ID = "consumerId";

    private static final Logger logger = LoggerFactory.getLogger(SamlUtils.class);

    private SamlUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static SamlInfo getSamlInfo(Assertion samlToken) {
        String uid = samlToken.getSubject().getNameID().getValue();
        String identType = null;
        String authLevel = null;
        String consumerId = null;
        List<Attribute> attributes = samlToken.getAttributeStatements().get(0).getAttributes();
        for (Attribute attribute : attributes) {
            String attributeName = attribute.getName();
            String attributeValue = attribute.getAttributeValues().get(0)
                    .getDOM().getFirstChild().getTextContent();

            if (IDENT_TYPE.equalsIgnoreCase(attributeName)) {
                identType = attributeValue;
            } else if (AUTHENTICATION_LEVEL.equalsIgnoreCase(attributeName)) {
                authLevel = attributeValue;
            } else if (CONSUMER_ID.equalsIgnoreCase(attributeName)) {
                consumerId = attributeValue;
            } else if (logger.isDebugEnabled()) {
                logger.debug("Skipping SAML Attribute name: {} value: {}", LoggerUtils.removeLineBreaks(attribute.getName()), LoggerUtils.removeLineBreaks(attributeValue)); //NOSONAR
            }
        }
        if (uid == null || identType == null || authLevel == null || consumerId == null) {
            throw new IllegalArgumentException("SAML assertion is missing mandatory attribute");
        }
        int iAuthLevel;

        try {
            iAuthLevel = Integer.parseInt(authLevel);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("AuthLevel attribute of SAML assertion is not a number", e);
        }

        return new SamlInfo(uid, identType, iAuthLevel, consumerId);
    }

    public static Assertion toSamlAssertion(String assertion) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(new ByteArrayInputStream(assertion.getBytes(StandardCharsets.UTF_8)));


            SamlAssertionWrapper assertionWrapper = new SamlAssertionWrapper(document.getDocumentElement());
            return assertionWrapper.getSaml2();
        } catch (WSSecurityException|ParserConfigurationException|IOException|SAXException e) {
            throw new IllegalArgumentException("Could not deserialize SAML assertion", e);
        }

    }

    public static String getSamlAssertionAsString(Assertion assertion) throws TransformerException {
        return getSamlAssertionAsString(assertion.getDOM());
    }

    public static String getSamlAssertionAsString(Element element) throws TransformerException {
        StringWriter writer = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

}
