package no.nav.vedtak.sts.client;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import org.apache.cxf.ws.security.trust.delegation.DelegationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OnBehalfOfWithOidcCallbackHandler implements CallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(OnBehalfOfWithOidcCallbackHandler.class);

    static Element getElement() throws IOException {
        return lagOnBehalfOfElement();
    }

    private static Element lagOnBehalfOfElement() throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(getOnBehalfOfString())));
            return document.getDocumentElement();
        } catch (ParserConfigurationException e) {
            Feil feil = StsFeil.FACTORY.klarteIkkeLageBuilder(e);
            feil.log(logger);
            throw feil.toException();
        } catch (SAXException e) {
            Feil feil = StsFeil.FACTORY.klarteIkkeLeseElement(e);
            feil.log(logger);
            throw feil.toException();
        }
    }

    private static String getOnBehalfOfString() {
        String base64encodedJTW = Base64.getEncoder().encodeToString(getJwtAsBytes());
        return "<wsse:BinarySecurityToken EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"urn:ietf:params:oauth:token-type:jwt\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" + base64encodedJTW + "</wsse:BinarySecurityToken>";
    }


    private static byte[] getJwtAsBytes() {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        String jwt = subjectHandler.getInternSsoToken();
        if(jwt != null) {
            return jwt.getBytes(StandardCharsets.UTF_8);
        } else if(subjectHandler.getSamlToken() != null) {
            // HACK Setter jwt til tom string. Kaller aldri til STS når SamlToken er tilstede (#see NAVSTSClient.requestSecurityToken())
            return "".getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalStateException("Har ikke en gyldig session.");
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof DelegationCallback) {
                DelegationCallback delegationCallback = (DelegationCallback) callback;
                delegationCallback.setToken(getElement());
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

}
