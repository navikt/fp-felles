package no.nav.vedtak.sikkerhet.jaspic;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.module.ServerAuthModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class OidcAuthConfigProvider implements AuthConfigProvider {

    private static final Environment ENV = Environment.current();

    private static final String CALLBACK_HANDLER_PROPERTY_NAME = "authconfigprovider.client.callbackhandler";
    private static final Logger log = LoggerFactory.getLogger(OidcAuthConfigProvider.class);

    private Map<String, String> providerProperties;
    private ServerAuthModule serverAuthModule;

    /**
     * Constructor with signature and implementation that's required by API.
     *
     * @param properties properties
     * @param factory    factory
     */
    public OidcAuthConfigProvider(Map<String, String> properties, AuthConfigFactory factory) {
        this.providerProperties = properties;

        // API requires self registration if factory is provided. Not clear
        // where the "layer" (2nd parameter)
        // and especially "appContext" (3rd parameter) values have to come from
        // at this place.
        if (factory != null) {
            factory.registerConfigProvider(this, null, null, "Auto registration");
        }
    }

    public OidcAuthConfigProvider(ServerAuthModule serverAuthModule) {
        log.trace("Instantiated");
        this.serverAuthModule = serverAuthModule;
    }

    /**
     * Not implemented
     */
    @Override
    public ClientAuthConfig getClientAuthConfig(String layer, String appContext, CallbackHandler handler) throws AuthException {
        return null;
    }

    /**
     * The actual factory method that creates the factory used to eventually obtain
     * the delegate for a SAM.
     */
    @Override
    public ServerAuthConfig getServerAuthConfig(String layer, String appContext, CallbackHandler handler) throws AuthException {
        log.trace("getServerAuthConfig");
        return new OidcServerAuthConfig(layer, appContext, handler == null ? createDefaultCallbackHandler() : handler, providerProperties,
                serverAuthModule);
    }

    @Override
    public void refresh() {
    }

    /**
     * Creates a default callback handler via the system property
     * "authconfigprovider.client.callbackhandler", as seemingly required by the API
     * (API uses wording "may" create default handler).
     *
     * @return an instance of the default call back handler
     * @throws AuthException ved feil
     */
    private CallbackHandler createDefaultCallbackHandler() throws AuthException {
        String callBackClassName = ENV.getProperty(CALLBACK_HANDLER_PROPERTY_NAME);

        if (callBackClassName == null) {
            throw new AuthException("No default handler set via system property: " + CALLBACK_HANDLER_PROPERTY_NAME);
        }

        try {
            return (CallbackHandler) Thread.currentThread().getContextClassLoader().loadClass(callBackClassName).getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }
}
