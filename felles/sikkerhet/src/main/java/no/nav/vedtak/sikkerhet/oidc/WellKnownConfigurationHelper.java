package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

public class WellKnownConfigurationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(WellKnownConfigurationHelper.class);
    private static final Environment ENV = Environment.current();

    private static Map<String, AuthorizationServerMetadata> wellKnownConfigMap = Collections.synchronizedMap(new LinkedHashMap<>());;

    public static synchronized AuthorizationServerMetadata getWellKnownConfig(String discoveryUrl, String proxyUrl) {
        if (wellKnownConfigMap.get(discoveryUrl) == null) {
            wellKnownConfigMap.put(discoveryUrl, retrieveAuthorizationServerMetadata(discoveryUrl, proxyUrl));
        }
        return wellKnownConfigMap.get(discoveryUrl);
    }

    public static Optional<String> getIssuerFra(String discoveryURL) {
        return getIssuerFra(discoveryURL, null);
    }

    public static Optional<String> getIssuerFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter issuer fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).getIssuer().getValue());
    }

    public static Optional<String> getJwksFra(String discoveryURL) {
        return getJwksFra(discoveryURL, null);
    }

    public static Optional<String> getJwksFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter jwki_uri fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).getJWKSetURI().toString());
    }

    public static Optional<URI> getTokenEndpointFra(String discoveryURL) {
        return getTokenEndpointFra(discoveryURL, null);
    }

    public static Optional<URI> getTokenEndpointFra(String discoveryURL, String proxyUrl) {
        LOG.debug("Henter token_endpoint fra {}", discoveryURL);
        return Optional.ofNullable(discoveryURL).map(u -> getWellKnownConfig(u, proxyUrl).getTokenEndpointURI());
    }

    private static AuthorizationServerMetadata retrieveAuthorizationServerMetadata(String discoveryURL, String proxyUrl) {
        try {
            LOG.debug("Henter well-known konfig fra '{}'", discoveryURL);
            var resourceRetriever = new DefaultResourceRetriever();
            Optional.ofNullable(proxyUrl)
                .map(URI::create)
                .ifPresent(u -> resourceRetriever.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(u.getHost(), u.getPort()))));
            var url = URI.create(discoveryURL).toURL();
            return AuthorizationServerMetadata.parse(resourceRetriever.retrieveResource(url).getContent());
        } catch (ParseException | IOException e) {
            throw new TekniskException("F-999999", String.format("Exception when retrieving metadata from issuer %s", discoveryURL), e);
        }
    }

    public static void setWellKnownConfig(String discoveryUrl, String jsonAsString) {
        guardForTestOnly();
        wellKnownConfigMap.computeIfAbsent(discoveryUrl, key -> {
            try {
                return AuthorizationServerMetadata.parse(jsonAsString);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Ugyldig json: ", e);
            }
        });
    }

    public static void unsetWellKnownConfig() {
        guardForTestOnly();
        wellKnownConfigMap = new HashMap<>();
    }

    private static void guardForTestOnly() {
        if (!ENV.isLocal()) {
            throw new IllegalStateException("Skal aldri kjøres i miljø!");
        }
    }
}
