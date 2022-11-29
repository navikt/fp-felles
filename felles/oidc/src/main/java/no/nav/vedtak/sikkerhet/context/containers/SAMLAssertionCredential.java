package no.nav.vedtak.sikkerhet.context.containers;

import no.nav.vedtak.exception.TekniskException;
import org.w3c.dom.Element;

import javax.security.auth.Destroyable;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

public class SAMLAssertionCredential implements Destroyable {

    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        TRANSFORMER_FACTORY = TransformerFactory.newInstance();
        try {
            TRANSFORMER_FACTORY.setFeature(FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private boolean destroyed;
    private String samlId;
    private String samlTokenAsString;
    private Element samlTokenAsElement;

    public SAMLAssertionCredential(Element samlTokenElement) {
        this.samlId = samlTokenElement.getAttribute("ID");
        this.samlTokenAsString = getSamlAssertionAsString(samlTokenElement);
        this.samlTokenAsElement = samlTokenElement;
    }

    @Override
    public void destroy() {
        samlTokenAsString = null;
        samlId = null;
        samlTokenAsElement = null;
        destroyed = true;
    }

    public String getSamlId() {
        if (destroyed) {
            throw new IllegalStateException("This credential is no longer valid");
        }
        return samlId;
    }

    public String getTokenAsString() {
        if (destroyed) {
            throw new IllegalStateException("This credential is no longer valid");
        }
        return samlTokenAsString;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String toString() {
        if (destroyed) {
            return "SAMLAssertionCredential[destroyed]";
        }
        return "SAMLAssertionCredential[xxxxxx]";
    }

    public Element getTokenAsElement() {
        return samlTokenAsElement;
    }

    private static String getSamlAssertionAsString(Element element) {
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new TekniskException("F-341074", "Kunne ikke gjøre SAML token om til streng", e);
        }
    }

}
