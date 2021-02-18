package no.nav.vedtak.sts.client;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStoreFactory;
import org.apache.cxf.ws.security.trust.STSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import no.nav.vedtak.util.env.Environment;

public class NAVSTSClient extends STSClient {
    private static final Environment ENV = Environment.current();

    private static final Logger logger = LoggerFactory.getLogger(NAVSTSClient.class);
    public static final String DISABLE_CACHE_KEY = "NAVSTSClient.DISABLE_CACHE";
    private static TokenStore tokenStore;
    private static SluttBruker systemSluttBruker = new SluttBruker(ENV.getProperty("systembruker.username"), IdentType.Systemressurs);

    private StsClientType type;

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
        var samlToken = subjectHandler.getSamlToken();
        String userId = subjectHandler.getUid();

        if (userId == null) {
            userId = "unauthenticated";
        }

        String key;
        SluttBruker principal;
        if (StsClientType.SYSTEM_SAML == type) {
            key = "systemSAML";
            principal = systemSluttBruker;
        } else {
            key = subjectHandler.getInternSsoToken();
            principal = SubjectHandler.getSubjectHandler().getSluttBruker();
        }

        if (samlToken != null) {
            SecurityToken token = new SecurityToken(samlToken.getSamlId(), samlToken.getTokenAsElement(), null);
            token.setPrincipal(principal);
            if (logger.isTraceEnabled()) {
                logger.trace("Will use SAML-token found in subjectHandler: {}", tokenToString(token));
            }
            return token;
        }

        if (Boolean.getBoolean(DISABLE_CACHE_KEY)) {
            logger.debug("Cache is disabled, fetching from STS for user {}", userId);
            SecurityToken token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            if (logger.isTraceEnabled()) {
                logger.trace("Retrived token from STS: {}", tokenToString(token));
            }
            return token;
        }

        ensureTokenStoreExists();

        if (key == null) {
            throw StsFeil.kanIkkeHenteSamlUtenOidcToken();
        }
        SecurityToken token = tokenStore.getToken(key);
        String keyUtenSignatur = stripJwtSignatur(key);
        if (token == null) {
            logger.debug("Missing token for user {}, cache key {}, fetching it from STS", userId, keyUtenSignatur); // NOSONAR
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            tokenStore.add(key, token);
        } else if (token.isExpired()) {
            logger.debug("Token for user {}, cache key {} is expired ({}) fetching a new one from STS", userId, keyUtenSignatur, token.getExpires()); // NOSONAR
            tokenStore.remove(key);
            token = super.requestSecurityToken(appliesTo, action, requestType, binaryExchange);
            token.setPrincipal(principal);
            tokenStore.add(key, token);
        } else {
            logger.debug("Retrived token for user {}, cache key {} from tokenStore", userId, keyUtenSignatur); // NOSONAR
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Retrived token: {}", tokenToString(token));
        }
        return token;
    }

    /**
     * A JWT consists of &lt;base64 encoded header&gt;.&lt;base64 encoded
     * body&gt;.&lt;base64 encoded signature&gt;
     *
     * @return if key is JWT - &lt;base64 encoded header&gt;.&lt;base64 encoded
     *         body&gt; <br>
     *         else - {@code key}
     */
    private static String stripJwtSignatur(String key) {
        final int lastDot = key.lastIndexOf('.');
        final int end = lastDot == -1 ? key.length() : lastDot;
        return key.substring(0, end);
    }

    private static String tokenToString(SecurityToken token) {
        return token.getClass().getSimpleName() + "<" +
                "id=" + token.getId() + ", "
                + "wsuId=" + token.getWsuId() + ", "
                + "principal=" + token.getPrincipal() + ", "
                + "created=" + token.getCreated() + ", "
                + "expires=" + token.getExpires() + ", "
                + "isExpired=" + token.isExpired() + ", "
                + ">";
    }

    private void ensureTokenStoreExists() {
        if (tokenStore == null) {
            try {
                createTokenStore();
            } catch (Exception e) {
                // for kompat cxf 3.4
                throw new IllegalStateException("Kan ikke opprette TokenStore", e);
            }
        }
    }

    private synchronized void createTokenStore() throws Exception {
        if (tokenStore == null) {
            logger.debug("Creating tokenStore");
            tokenStore = TokenStoreFactory.newInstance().newTokenStore(SecurityConstants.TOKEN_STORE_CACHE_INSTANCE, message);
        }
    }
}