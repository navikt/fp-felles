package no.nav.vedtak.sikkerhet.loginmodule.saml;

import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.context.containers.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.context.containers.ConsumerId;
import no.nav.vedtak.sikkerhet.context.containers.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.loginmodule.LoginModuleBase;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This <code>LoginModule</code> authenticates users using
 * the custom SAML token.
 */
public class SamlLoginModule extends LoginModuleBase {

    private static final String IDENT_TYPE = "identType";
    private static final String AUTHENTICATION_LEVEL = "authenticationLevel";
    private static final String CONSUMER_ID = "consumerId";

    private static final Logger LOG = LoggerFactory.getLogger(SamlLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;

    private SamlInfo samlInfo;
    private Assertion samlAssertion;

    private SluttBruker sluttBruker;
    private AuthenticationLevelCredential authenticationLevelCredential;
    private SAMLAssertionCredential samlAssertionCredential;
    private ConsumerId consumerId;

    public SamlLoginModule() {
        super(LOG);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        LOG.trace("Initialize loginmodule");
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        LOG.trace("Initializing with subject: {} callbackhandler: {}", subject, callbackHandler);
    }

    @Override
    public boolean login() throws LoginException {
        try {
            LOG.trace("enter login");
            PasswordCallback passwordCallback = new PasswordCallback("Return SAML-assertion as password", false);
            callbackHandler.handle(new Callback[]{passwordCallback});

            samlAssertion = toSamlAssertion(new String(passwordCallback.getPassword()));
            samlInfo = getSamlInfo(samlAssertion);
            setLoginSuccess(true);
            LOG.trace("Login successful for user {} with authentication level {}", samlInfo.uid(), samlInfo.authLevel());

            return true;
        } catch (Exception e) {
            samlAssertion = null;
            samlInfo = null;
            LOG.trace("leave login: exception");
            throw new LoginException(e.toString());// NOPMD
        }
    }

    @Override
    public void doCommit() throws LoginException {
        sluttBruker = new SluttBruker(samlInfo.uid(), getIdentType());
        authenticationLevelCredential = new AuthenticationLevelCredential(samlInfo.authLevel());
        Element samlElement = samlAssertion.getDOM();
        samlAssertionCredential = new SAMLAssertionCredential(samlElement);
        consumerId = new ConsumerId(samlInfo.consumerId());

        subject.getPrincipals().add(sluttBruker);
        subject.getPrincipals().add(consumerId);
        subject.getPublicCredentials().add(authenticationLevelCredential);
        subject.getPublicCredentials().add(samlAssertionCredential);

        LOG.trace("Login committed for subject with uid: {} authentication level: {} and consumerId: {}",
            sluttBruker.getName(), authenticationLevelCredential.getAuthenticationLevel(), consumerId);
    }

    private IdentType getIdentType() throws LoginException {
        IdentType identType;
        try {
            identType = IdentType.valueOf(samlInfo.identType());
        } catch (IllegalArgumentException e) {
            LoginException le = new LoginException("Could not commit. Unknown ident type: " + samlInfo.identType() + " " + e);
            le.initCause(e);
            throw le;
        }
        return identType;
    }

    @Override
    protected void cleanUpSubject() {
        if (!subject.isReadOnly()) {
            subject.getPrincipals().remove(sluttBruker);
            subject.getPrincipals().remove(consumerId);
            subject.getPublicCredentials().remove(samlAssertionCredential);
            subject.getPublicCredentials().remove(authenticationLevelCredential);
        }
    }

    @Override
    protected void cleanUpLocalState() throws LoginException {
        // Set during login()
        samlInfo = null;
        samlAssertion = null;

        // Set during commit()
        if (sluttBruker != null) {
            sluttBruker.destroy();
        }
        sluttBruker = null;

        if (consumerId != null) {
            consumerId.destroy();
        }
        consumerId = null;

        if (authenticationLevelCredential != null) {
            authenticationLevelCredential.destroy();
        }
        authenticationLevelCredential = null;

        if (samlAssertionCredential != null) {
            samlAssertionCredential.destroy();
        }
        samlAssertionCredential = null;
    }

    private static SamlInfo getSamlInfo(Assertion samlToken) {
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
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping SAML Attribute name: {} value: {}", LoggerUtils.removeLineBreaks(attribute.getName()),
                    LoggerUtils.removeLineBreaks(attributeValue)); // NOSONAR
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

    private static Assertion toSamlAssertion(String assertion) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Document document = documentBuilder.parse(new ByteArrayInputStream(assertion.getBytes(StandardCharsets.UTF_8)));

            SamlAssertionWrapper assertionWrapper = new SamlAssertionWrapper(document.getDocumentElement());
            return assertionWrapper.getSaml2();
        } catch (WSSecurityException | ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalArgumentException("Could not deserialize SAML assertion", e);
        }

    }

}
