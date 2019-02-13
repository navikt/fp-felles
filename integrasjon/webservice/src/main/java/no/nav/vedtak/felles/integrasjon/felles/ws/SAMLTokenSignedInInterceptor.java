package no.nav.vedtak.felles.integrasjon.felles.ws;

import static no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames.SAML;

import java.util.Map;
import java.util.Properties;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.xml.transform.TransformerException;

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

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.loginmodule.LoginModuleFeil;
import no.nav.vedtak.sikkerhet.loginmodule.SamlUtils;

/**
 * CXF Soap interceptor som validerer SAML-token og logger caller inn i containeren.
 */
public class SAMLTokenSignedInInterceptor extends WSS4JInInterceptor {

    public SAMLTokenSignedInInterceptor() {
        super();
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
        signatureProperties.setProperty("org.apache.ws.security.crypto.merlin.truststore.file", System.getProperty("javax.net.ssl.trustStore"));
        signatureProperties.setProperty("org.apache.ws.security.crypto.merlin.truststore.password", System.getProperty("javax.net.ssl.trustStorePassword")); // NOSONAR ikke et hardkodet passord

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

        // No need for bean.destroy(instance) since it's ApplicationScoped
        LoginContextConfiguration loginContextConfiguration = CDI.current().select(LoginContextConfiguration.class).get();
        try {
            String result = SamlUtils.getSamlAssertionAsString(assertion);
            LoginContext loginContext = createLoginContext(SAML, loginContextConfiguration, result);
            loginContext.login();
            MDCOperations.putUserId(SubjectHandler.getSubjectHandler().getUid());
            MDCOperations.putConsumerId(SubjectHandler.getSubjectHandler().getConsumerId());
        } catch (LoginException | TransformerException e) {
            throw LoginModuleFeil.FACTORY.feiletInnlogging(e).toException();
        }
    }

    private LoginContext createLoginContext(LoginConfigNames loginConfigName, LoginContextConfiguration loginContextConfiguration, String assertion) {
        CallbackHandler callbackHandler = new PaswordCallbackHandler(assertion);
        try {
            return new LoginContext(loginConfigName.name(), new Subject(), callbackHandler, loginContextConfiguration);
        } catch (LoginException le) {
            throw LoginModuleFeil.FACTORY.kunneIkkeFinneLoginmodulen(loginConfigName.name(), le).toException();
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
}
