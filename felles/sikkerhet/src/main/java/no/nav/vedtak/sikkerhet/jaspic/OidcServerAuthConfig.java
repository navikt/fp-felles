package no.nav.vedtak.sikkerhet.jaspic;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;

public class OidcServerAuthConfig implements ServerAuthConfig {

    private final String messageLayer;
    private final String appContext;
    private final CallbackHandler callbackHandler;
    @SuppressWarnings("unused")
    private final Map<String, String> providerProperties;
    private final ServerAuthModule serverAuthModule;

    public OidcServerAuthConfig(String messageLayer, String appContext, CallbackHandler callbackHandler, Map<String, String> providerProperties,
            ServerAuthModule serverAuthModule) {
        this.messageLayer = messageLayer;
        this.appContext = appContext;
        this.callbackHandler = callbackHandler;
        this.providerProperties = providerProperties;
        this.serverAuthModule = serverAuthModule;
    }

    @Override
    public ServerAuthContext getAuthContext(String authContextID, Subject serviceSubject, @SuppressWarnings("rawtypes") Map properties)
            throws AuthException {
        return new OidcServerAuthContext(callbackHandler, serverAuthModule);
    }

    @Override
    public String getMessageLayer() {
        return messageLayer;
    }

    @Override
    public String getAppContext() {
        return appContext;
    }

    @Override
    public String getAuthContextID(MessageInfo messageInfo) {
        return appContext;
    }

    @Override
    public void refresh() {
        // NOOP
    }

    @Override
    public boolean isProtected() {
        return false;
    }
}
