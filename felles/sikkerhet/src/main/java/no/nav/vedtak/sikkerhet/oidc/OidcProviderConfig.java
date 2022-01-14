package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getIssuerFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getJwksFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getTokenEndpointFra;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.isso.OpenAMHelper;

public final class OidcProviderConfig {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(OidcProviderConfig.class);

    public static final String OPEN_AM_WELL_KNOWN_URL = "oidc.open.am.well.known.url";
    public static final String OPEN_AM_CLIENT_ID = "oidc.open.am.client.id";
    public static final String OPEN_AM_CLIENT_SECRET = "oidc.open.am.client.secret";

    private static final String STS_WELL_KNOWN_URL = "oidc.sts.well.known.url";

    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url"; // naiserator
    private static final String LOGINSERVICE_CLIENT_ID = "loginservice.idporten.audience"; // naiserator

    private static final String AZURE_WELL_KNOWN_URL = "azure.app.well.known.url"; // naiserator
    private static final String AZURE_CLIENT_ID = "azure.app.client.id"; // naiserator

    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url"; // naiserator
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id"; // naiserator

    private static volatile OidcProviderConfig instance; // NOSONAR
    private static Set<OidcProvider> providers = new HashSet<>();

    private OidcProviderConfig() {
        this(init());
    }

    private OidcProviderConfig(Set<OidcProvider> providers) {
        OidcProviderConfig.providers = providers;
    }

    public static OidcProviderConfig instance() {
        var inst= instance;
        if (inst == null) {
            inst = new OidcProviderConfig();
            instance = inst;
        }
        return inst;
    }

    public Set<OidcProvider> getOidcProviders() {
        return providers;
    }

    public Optional<OidcProvider> getOidcProvider(OidcProviderType type) {
        return providers.stream().filter(p -> p.getType().equals(type)).findFirst();
    }

    private static Set<OidcProvider> init() {
        if (providers.isEmpty()) {
            LOG.debug("Henter ID providere.");
            return providers = hentConfig();
        }
        return providers;
    }

    private static Set<OidcProvider> hentConfig() {
        Set<OidcProvider> idProviderConfigs = new HashSet<>();

        // OpenAM
        idProviderConfigs.add(createOpenAmConfiguration(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL)));

        // OIDC STS
        if (ENV.getProperty(STS_WELL_KNOWN_URL) != null || ENV.getProperty("oidc.sts.issuer.url") != null) { // Det er kanskje noen apper som ikke bruker STS token validering??
            idProviderConfigs.add(createStsConfiguration(ENV.getProperty(STS_WELL_KNOWN_URL)));
        }

        // Azure
        var azureKonfigUrl = ENV.getProperty(AZURE_WELL_KNOWN_URL);
        if (azureKonfigUrl != null) {
            LOG.debug("Oppretter AzureAD konfig fra '{}'", azureKonfigUrl);
            idProviderConfigs.add(createAzureAppConfiguration(azureKonfigUrl));
        }

        // Loginservice
        var loginserviceKonfigUrl = ENV.getProperty(LOGINSERVICE_IDPORTEN_DISCOVERY_URL);
        if (loginserviceKonfigUrl != null) {
            LOG.debug("Oppretter Loginservice konfig fra '{}'", loginserviceKonfigUrl);
            idProviderConfigs.add(createLoginServiceConfiguration(loginserviceKonfigUrl));
        }

        // TokenX
        var tokenxKonfigUrl = ENV.getProperty(TOKEN_X_WELL_KNOWN_URL);
        if (tokenxKonfigUrl != null) {
            LOG.debug("Oppretter TokenX konfig fra '{}'", tokenxKonfigUrl);
            idProviderConfigs.add(createTokenXConfiguration(tokenxKonfigUrl));
        }

        LOG.info("ID Providere som er tilgjengelig: {}", idProviderConfigs.stream()
            .map(OidcProvider::getIssuer)
            .map(URL::getHost)
            .collect(Collectors.joining(", ")));

        return idProviderConfigs;
    }

    private static OidcProvider createOpenAmConfiguration(String wellKnownUrl) {
        LOG.debug("Oppretter OpenAM konfig fra '{}'", wellKnownUrl);
        return createConfiguration(OidcProviderType.ISSO,
            "oidc_openam",
            getIssuerFra(wellKnownUrl).orElse(OpenAMHelper.getIssoIssuerUrl()),
            getJwksFra(wellKnownUrl).orElse(OpenAMHelper.getIssoJwksUrl()),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            false,
            OpenAMHelper.getIssoUserName(),
            true);
    }

    private static OidcProvider createStsConfiguration(String wellKnownUrl) {
        LOG.debug("Oppretter OpenAM konfig fra '{}'", wellKnownUrl);
        return createConfiguration(OidcProviderType.STS,
            "oidc_sts",
            getIssuerFra(wellKnownUrl).orElse(ENV.getProperty("oidc.sts.issuer.url")),
            getJwksFra(wellKnownUrl).orElse(ENV.getProperty("oidc.sts.jwks.url")),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            false,
            "Client name is not used for OIDC STS",
            true);
    }

    private static OidcProvider createAzureAppConfiguration(final String wellKnownUrl) {
        return createConfiguration(OidcProviderType.AZUREAD,
            "oidc_azure",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            !ENV.isLocal(),
            ENV.getRequiredProperty(AZURE_CLIENT_ID),
            true);
    }

    private static OidcProvider createLoginServiceConfiguration(String wellKnownUrl) {
        return createConfiguration(OidcProviderType.LOGINSERVICE,
            "oidc_loginservice",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            !ENV.isLocal(),
            ENV.getRequiredProperty(LOGINSERVICE_CLIENT_ID),
            false);
    }

    private static OidcProvider createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration(OidcProviderType.TOKENX,
            "oidc_tokenx",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            false,
            ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
            false);
    }

    private static OidcProvider createConfiguration(OidcProviderType type,
                                                    String providerName,
                                                    String issuer,
                                                    String jwks,
                                                    URI tokenEndpoint,
                                                    boolean useProxyForJwks,
                                                    String clientName,
                                                    boolean skipAudienceValidation) {
        return Optional.ofNullable(clientName)
                .map(c -> new OidcProvider(
                        type,
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        tokenEndpoint,
                        useProxyForJwks,
                        c,
                    30,
                        skipAudienceValidation))
                .orElseThrow();
    }

    private static URL url(String url, String key, String providerName) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new TekniskException("F-644196",
                    String.format("Syntaksfeil i token validator konfigurasjonen av '%s' for '%s'", key, providerName), e);
        }
    }
}
