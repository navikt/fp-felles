package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
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

    public static AuthorizationServerMetadata getWellKnownConfig(String discoveryUrl) {
        return wellKnownConfigMap.computeIfAbsent(discoveryUrl, WellKnownConfigurationHelper::retrieveAuthorizationServerMetadata);
    }

    public static Optional<String> getIssuerFra(String discoveryURL) {
        LOG.debug("Henter issuer fra {}", discoveryURL);
        return discoveryURL != null ?
            Optional.of(getWellKnownConfig(discoveryURL).getIssuer().getValue()) :
            Optional.empty();
    }

    public static Optional<String> getJwksFra(String discoveryURL) {
        LOG.debug("Henter jwki_uri fra {}", discoveryURL);
        return discoveryURL != null ?
            Optional.of(getWellKnownConfig(discoveryURL).getJWKSetURI().toString()) :
            Optional.empty();
    }

    public static Optional<URI> getTokenEndpointFra(String discoveryURL) {
        LOG.debug("Henter token_endpoint fra {}", discoveryURL);
        return discoveryURL != null ?
            Optional.of(getWellKnownConfig(discoveryURL).getTokenEndpointURI()) :
            Optional.empty();
    }

    private static AuthorizationServerMetadata retrieveAuthorizationServerMetadata(String discoveryURL) {
        try {
            LOG.debug("Henter well-known konfig fra '{}'", discoveryURL);
            var resourceRetriever = new DefaultResourceRetriever();
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
