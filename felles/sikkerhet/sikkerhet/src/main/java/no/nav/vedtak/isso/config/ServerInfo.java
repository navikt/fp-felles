package no.nav.vedtak.isso.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.util.env.Environment;

public final class ServerInfo {

    public static final String PROPERTY_KEY_LOADBALANCER_URL = "loadbalancer.url";
    public static final String CALLBACK_ENDPOINT = "/cb";
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(ServerInfo.class);
    private static ServerInfo instance;
    private String schemeHostPort = schemeHostPortFromSystemProperties();
    private boolean isUsingTLS = schemeHostPort.toLowerCase().startsWith("https");
    private String relativeCallbackUrl;
    private String callbackUrl;
    private String cookiePath;
    private String cookieDomain = cookieDomain(schemeHostPort);

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

    private static String schemeHostPortFromSystemProperties() {

        return ENV.getRequiredProperty(PROPERTY_KEY_LOADBALANCER_URL,
            () -> ServerInfoFeil.manglerNødvendigSystemProperty(PROPERTY_KEY_LOADBALANCER_URL));
    }

    private static String cookieDomain(String schemeHostPort) {
        return removeSchemeAndPort(schemeHostPort);
    }

    private static String removeSchemeAndPort(String schemeHostPort) {
        Pattern pattern = Pattern.compile("^https?://([\\w\\-.]+)(:\\d+)?$");
        Matcher m = pattern.matcher(schemeHostPort);
        if (m.find()) {
            String hostname = m.group(1);
            if (hostname.split("\\.").length >= 3) {
                return hostname.substring(hostname.indexOf('.') + 1);
            } else {
                ServerInfoFeil.uventetHostFormat(hostname).log(LOG);
                return null; // null er det strengeste i cookie domain, betyr 'kun denne server'
            }
        } else {
            throw ServerInfoFeil.ugyldigSystemProperty(PROPERTY_KEY_LOADBALANCER_URL, schemeHostPort);
        }
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

    public String getCookiePath() {
        if (cookiePath == null) {
            cookiePath = ContextPathHolder.instance().getCookiePath();
        }

        return cookiePath;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[schemeHostPort=" + schemeHostPort + ", isUsingTLS=" + isUsingTLS
            + ", relativeCallbackUrl=" + relativeCallbackUrl + ", callbackUrl=" + callbackUrl + ", cookieDomain="
            + cookieDomain + "]";
    }

}
