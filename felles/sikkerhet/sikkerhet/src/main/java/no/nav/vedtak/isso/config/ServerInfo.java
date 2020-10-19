package no.nav.vedtak.isso.config;

import java.net.URI;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InternetDomainName;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.util.env.Environment;

public final class ServerInfo {

    private static final Environment ENV = Environment.current();

    private static final Logger LOG = LoggerFactory.getLogger(ServerInfo.class);
    public static final String PROPERTY_KEY_LOADBALANCER_URL = "loadbalancer.url";
    public static final String CALLBACK_ENDPOINT = "/cb";

    private String schemeHostPort = schemeHostPortFromSystemProperties();
    private boolean isUsingTLS = schemeHostPort.toLowerCase().startsWith("https");
    private String relativeCallbackUrl;
    private String callbackUrl;
    private String cookieDomain = cookieDomain(schemeHostPort);

    private static ServerInfo instance;

    ServerInfo() {
    }

    public static synchronized ServerInfo instance() {
        if (instance == null) {
            instance = new ServerInfo();
        }
        return instance;
    }

    static synchronized void clearInstance() {
        instance = null;
    }

    public String getSchemeHostPort() {
        return schemeHostPort;
    }

    public boolean isUsingTLS() {
        return isUsingTLS;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public String getCallbackUrl() {
        if (callbackUrl == null) {
            callbackUrl = schemeHostPort + getRelativeCallbackUrl();
        }
        return callbackUrl;
    }

    public String getRelativeCallbackUrl() {
        if (relativeCallbackUrl == null) {
            relativeCallbackUrl = ContextPathHolder.instance().getContextPath() + CALLBACK_ENDPOINT;
        }
        return relativeCallbackUrl;
    }

    private static String schemeHostPortFromSystemProperties() {

        return ENV.getRequiredProperty(PROPERTY_KEY_LOADBALANCER_URL,
                ServerInfoFeil.FACTORY.manglerNødvendigSystemProperty(PROPERTY_KEY_LOADBALANCER_URL));
    }

    private static String cookieDomain(String schemeHostPort) {
        return removeSchemeAndPort(schemeHostPort);
    }

    private static String removeSchemeAndPort(String schemeHostPort) {
        return Optional.ofNullable(domainName(schemeHostPort))
                .filter(not(InternetDomainName::isTopPrivateDomain))
                .filter(InternetDomainName::isUnderPublicSuffix)
                .map(InternetDomainName::topPrivateDomain)
                .map(Object::toString)
                .orElse(null);
    }

    private static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    private static InternetDomainName domainName(String schemeHostPort) {
        try {
            return Optional.ofNullable(schemeHostPort)
                    .map(URI::create)
                    .map(URI::getHost)
                    .map(InternetDomainName::from)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            LOG.warn("Uventet format for host,  kunne ikke parse {} til domain name", schemeHostPort);
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[schemeHostPort=" + schemeHostPort + ", isUsingTLS=" + isUsingTLS
                + ", relativeCallbackUrl=" + relativeCallbackUrl + ", callbackUrl=" + callbackUrl + ", cookieDomain="
                + cookieDomain + "]";
    }

}
