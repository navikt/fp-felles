package no.nav.vedtak.felles.integrasjon.felles.ws;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.principal.SAMLTokenPrincipal;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.w3c.dom.Element;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;

/**
 * CXF Soap interceptor som validerer SAML-token og logger caller inn i
 * containeren. Legger også til en Interceptor som logger ut igjen fra
 * containeren.
 *
 * @see SAMLLogoutInterceptor
 */
public class SAMLTokenSignedInInterceptor extends WSS4JInInterceptor {

    private static final String LOGIN_CONFIG_NAME = "SAML";

    private static final TransformerFactory transformerFactory;

    private LoginContextConfiguration loginContextConfiguration = new LoginContextConfiguration();

    static {
        transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final Environment ENV = Environment.current();

    public SAMLTokenSignedInInterceptor() {
        setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
    }

    public SAMLTokenSignedInInterceptor(boolean ignore) {
        super(ignore);
        setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
    }

    public SAMLTokenSignedInInterceptor(Map<String, Object> properties) {
        super(properties);
        setProperty(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED);
    }

    @Override
    public Crypto loadSignatureCrypto(RequestData requestData) throws WSSecurityException {

        Properties signatureProperties = new Properties();
        signatureProperties.setProperty("org.apache.ws.security.crypto.merlin.truststore.file", ENV.getProperty("javax.net.ssl.trustStore"));
        signatureProperties.setProperty("org.apache.ws.security.crypto.merlin.truststore.password",
                ENV.getProperty("javax.net.ssl.trustStorePassword")); // NOSONAR ikke et hardkodet passord

        Crypto crypto = CryptoFactory.getInstance(signatureProperties);
        cryptos.put(WSHandlerConstants.SIG_PROP_REF_ID, crypto);

        return crypto;
    }

    @Override
    public void handleMessage(SoapMessage msg) {
        super.handleMessage(msg);

        SecurityContext securityContext = msg.get(SecurityContext.class);
        SAMLTokenPrincipal samlTokenPrincipal = (SAMLTokenPrincipal) securityContext.getUserPrincipal();
        Assertion assertion = samlTokenPrincipal.getToken().getSaml2();

        try {
            String result = getSamlAssertionAsString(assertion);
            LoginContext loginContext = createLoginContext(loginContextConfiguration, result);
            loginContext.login();
            msg.getInterceptorChain().add(new SAMLLogoutInterceptor(loginContext));
            MDCOperations.putUserId(SubjectHandler.getSubjectHandler().getUid());
            MDCOperations.putConsumerId(SubjectHandler.getSubjectHandler().getConsumerId());
        } catch (LoginException | TransformerException e) {
            throw new TekniskException("F-499051", "Noe gikk galt ved innlogging", e);
        }
    }

    private static LoginContext createLoginContext(LoginContextConfiguration loginContextConfiguration, String assertion) {
        CallbackHandler callbackHandler = new PaswordCallbackHandler(assertion);
        try {
            return new LoginContext(LOGIN_CONFIG_NAME, new Subject(), callbackHandler, loginContextConfiguration);
        } catch (LoginException le) {
            throw new TekniskException("F-651753", String.format("Kunne ikke finne konfigurasjonen for %s", LOGIN_CONFIG_NAME), le);
        }
    }

    private static class PaswordCallbackHandler implements CallbackHandler {
        private final String assertion;

        public PaswordCallbackHandler(String assertion) {
            this.assertion = assertion;
        }

        @Override
        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(assertion.toCharArray());
                } else {
                    // Should never happen
                    throw new UnsupportedCallbackException(callback, PasswordCallback.class + " is the only supported Callback");
                }
            }
        }
    }

    private static String getSamlAssertionAsString(Assertion assertion) throws TransformerException {
        return getSamlAssertionAsString(assertion.getDOM());
    }

    private static String getSamlAssertionAsString(Element element) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }
}
