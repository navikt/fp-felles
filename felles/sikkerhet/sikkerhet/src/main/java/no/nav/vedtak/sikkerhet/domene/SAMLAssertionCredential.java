package no.nav.vedtak.sikkerhet.domene;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import java.io.StringWriter;

import javax.security.auth.Destroyable;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class SAMLAssertionCredential implements Destroyable {
    
    private static final TransformerFactory transformerFactory;

    static {
        transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
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
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw SAMLLoginFeil.FEIL.kunneIkkeGjøreSamlTokenOmTilStreng(e).toException();
        }
    }
    
    public interface SAMLLoginFeil extends DeklarerteFeil {

        SAMLLoginFeil FEIL = FeilFactory.create(SAMLLoginFeil.class);

        @TekniskFeil(feilkode = "F-341074", feilmelding = "Kunne ikke gjøre SAML token om til streng ", logLevel = ERROR)
        Feil kunneIkkeGjøreSamlTokenOmTilStreng(Exception e);

    }
}
