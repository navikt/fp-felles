package no.nav.vedtak.sts.client;

import no.nav.vedtak.konfig.PropertyUtil;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory;
import org.apache.cxf.ws.security.trust.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class NAVSTSClient extends STSClient {
    private static final Logger logger = LoggerFactory.getLogger(NAVSTSClient.class);
    public static final String DISABLE_CACHE_KEY = "NAVSTSClient.DISABLE_CACHE";
    private static TokenStore tokenStore;
    private static SluttBruker systemSluttBruker = new SluttBruker(PropertyUtil.getProperty("systembruker.username"), IdentType.Systemressurs);

    private StsClientType type;

    public enum StsClientType {
        SYSTEM_SAML,
        SECURITYCONTEXT_TIL_SAML
    }

    public NAVSTSClient(Bus b, StsClientType type) {
        super(b);
        this.type = type;
    }

    @Override
    protected boolean useSecondaryParameters() {
        return false;
    }

    @Override
    public SecurityToken requestSecurityToken(String appliesTo, String action, String requestType, String binaryExchange) throws Exception {
        final SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();
        final Element samlToken = subjectHandler.getSamlToken();
        String userId = subjectHandler.getUid();

        if(userId == null){
            userId = "unauthenticated";
        }

        String key;
        SluttBruker principal;
        if(StsClientType.SYSTEM_SAML == type) {
            key = "systemSAML";
            principal = systemSluttBruker;
        } else {
            key = subjectHandler.getInternSsoToken();
            principal = SubjectHandler.getSubjectHandler().getSluttBruker();
        }

        if(samlToken != null) {
            SecurityToken token = new SecurityToken(samlToken.getAttribute("ID"), samlToken, null);
            token.setPrincipal(principal);
            if(logger.isTraceEnabled()){
                logger.trace("Will use SAML-token found in subjectHandler: {}", tokenToString(token));
            }
            return token;
        }

        if (Boolean.getBoolean(DISABLE_CACHE_KEY)) {
            logger.debug("Cache is disabled, fetching from STS for user {}", userId);
            SecurityToken token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            if(logger.isTraceEnabled()){
                logger.trace("Retrived token from STS: {}", tokenToString(token));
            }
            return token;
        }

        ensureTokenStoreExists();

        if (key == null) {
            throw StsFeil.FACTORY.kanIkkeHenteSamlUtenOidcToken().toException();
        }
        SecurityToken token = tokenStore.getToken(key);
        String keyUtenSignatur = stripJwtSignatur(key);
        if (token == null) {
            logger.debug("Missing token for user {}, cache key {}, fetching it from STS", userId, keyUtenSignatur);  //NOSONAR
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            tokenStore.add(key, token);
        } else if (token.isExpired()) {
            logger.debug("Token for user {}, cache key {} is expired ({}) fetching a new one from STS", userId, keyUtenSignatur, token.getExpires());  //NOSONAR
            tokenStore.remove(key);
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            tokenStore.add(key, token);
        } else {
            logger.debug("Retrived token for user {}, cache key {} from tokenStore", userId, keyUtenSignatur);  //NOSONAR
        }
        if(logger.isTraceEnabled()){
            logger.trace("Retrived token: {}", tokenToString(token));
        }
        return token;
    }

    /**
     * A JWT consists of &lt;base64 encoded header&gt;.&lt;base64 encoded body&gt;.&lt;base64 encoded signature&gt;
     * @return if key is JWT - &lt;base64 encoded header&gt;.&lt;base64 encoded body&gt; <br>
     *     else -  {@code key}
     */
    public String stripJwtSignatur(String key) {
        final int lastDot = key.lastIndexOf('.');
        final int end = lastDot == -1 ? key.length() : lastDot;
        return key.substring(0, end);
    }

    private String tokenToString(SecurityToken token) {
        return token.getClass().getSimpleName() + "<" + //$NON-NLS-1$
                "id=" + token.getId() + ", " //$NON-NLS-2$ //$NON-NLS-3$
                + "wsuId=" + token.getWsuId() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "principal=" + token.getPrincipal() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "created=" + token.getCreated() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "expires=" + token.getExpires() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "isExpired=" + token.isExpired() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }

    private void ensureTokenStoreExists() {
        if (tokenStore == null) {
            createTokenStore();
        }
    }

    private synchronized void createTokenStore() {
        if (tokenStore == null) {
            logger.debug("Creating tokenStore");
            tokenStore = TokenStoreFactory.newInstance().newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message);
        }
    }
}