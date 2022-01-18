package no.nav.vedtak.sikkerhet.oidc;

import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getAuthorizationEndpointFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getIssuerFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getJwksFra;
import static no.nav.vedtak.sikkerhet.oidc.WellKnownConfigurationHelper.getTokenEndpointFra;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String STS_CONFIG_ISSUER = "oidc.sts.openid.config.issuer";
    private static final String STS_CONFIG_JWKS_URI = "oidc.sts.openid.config.jwks.uri";
    private static final String STS_CONFIG_TOKEN_ENDPOINT = "oidc.sts.openid.config.token.endpoint";

    private static final String LOGINSERVICE_IDPORTEN_DISCOVERY_URL = "loginservice.idporten.discovery.url"; // naiserator
    private static final String LOGINSERVICE_CLIENT_ID = "loginservice.idporten.audience"; // naiserator

    private static final String AZURE_WELL_KNOWN_URL = "azure.app.well.known.url"; // naiserator
    private static final String AZURE_CONFIG_ISSUER = "azure.openid.config.issuer"; // naiserator
    private static final String AZURE_CONFIG_JWKS_URI = "azure.openid.config.jwks.uri"; // naiserator
    private static final String AZURE_CONFIG_TOKEN_ENDPOINT = "azure.openid.config.token.endpoint"; // naiserator
    private static final String AZURE_CLIENT_ID = "azure.app.client.id"; // naiserator
    private static final String AZURE_HTTP_PROXY = "azure.http.proxy"; // settes ikke av naiserator

    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url"; // naiserator
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id"; // naiserator

    private static final String PROXY_KEY = "proxy.url"; // FP-oppsett lite brukt
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";

    private static volatile OidcProviderConfig instance; // NOSONAR
    private static Set<OidcProvider> providers = new HashSet<>();

    private OidcProviderConfig() {
        this(init());
    }

    private OidcProviderConfig(Set<OidcProvider> providers) {
        OidcProviderConfig.providers = providers;
    }

    public static synchronized OidcProviderConfig instance() {
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
        if (ENV.getProperty(STS_WELL_KNOWN_URL) != null || ENV.getProperty("oidc.sts.issuer.url") != null || ENV.getProperty(STS_CONFIG_ISSUER) != null) { // Det er kanskje noen apper som ikke bruker STS token validering??
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
            .map(OidcProvider::getType)
            .map(OidcProviderType::name)
            .collect(Collectors.joining(", ")));

        return idProviderConfigs;
    }

    private static OidcProvider createOpenAmConfiguration(String wellKnownUrl) {
        LOG.debug("Oppretter OpenAM konfig fra '{}'", wellKnownUrl);
        return createConfiguration(OidcProviderType.ISSO,
            "oidc_openam",
            getIssuerFra(wellKnownUrl).orElseGet(OpenAMHelper::getIssoIssuerUrl),
            getJwksFra(wellKnownUrl).orElseGet(OpenAMHelper::getIssoJwksUrl),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            getAuthorizationEndpointFra(wellKnownUrl).orElse(null),
            false, null,
            OpenAMHelper.getIssoUserName(),
            true);
    }

    private static OidcProvider createStsConfiguration(String wellKnownUrl) {
        LOG.debug("Oppretter STS konfig fra '{}'", wellKnownUrl);
        return createConfiguration(OidcProviderType.STS,
            "oidc_sts",
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_ISSUER))
                .or(() -> getIssuerFra(wellKnownUrl))
                .orElseGet(() -> ENV.getProperty("oidc.sts.issuer.url")),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_JWKS_URI))
                .or(() -> getJwksFra(wellKnownUrl))
                .orElseGet(() -> ENV.getProperty("oidc.sts.jwks.url")),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_TOKEN_ENDPOINT)).map(URI::create)
                .or(() -> getTokenEndpointFra(wellKnownUrl)).orElse(null),
            null,
            false, null,
            "Client name is not used for OIDC STS",
            true);
    }

    @SuppressWarnings("unused")
    private static OidcProvider createAzureAppConfiguration(String wellKnownUrl) {
        var useProxy = ENV.isLocal() ? null : ENV.getProperty(AZURE_HTTP_PROXY, getDefaultProxy());
        return createConfiguration(OidcProviderType.AZUREAD,
            "oidc_azure",
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_ISSUER))
                .orElseGet(() -> getIssuerFra(wellKnownUrl, useProxy).orElse(null)),
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_JWKS_URI))
                .orElseGet(() -> getJwksFra(wellKnownUrl, useProxy).orElse(null)),
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_TOKEN_ENDPOINT)).map(URI::create)
                .orElseGet(() -> getTokenEndpointFra(wellKnownUrl, useProxy).orElse(null)),
            null,
            !ENV.isLocal(), useProxy,
            ENV.getRequiredProperty(AZURE_CLIENT_ID),
            true);
    }

    private static OidcProvider createLoginServiceConfiguration(String wellKnownUrl) {
        var useProxy = ENV.isLocal() ? null : getDefaultProxy();
        return createConfiguration(OidcProviderType.LOGINSERVICE,
            "oidc_loginservice",
            getIssuerFra(wellKnownUrl, useProxy).orElseThrow(),
            getJwksFra(wellKnownUrl, useProxy).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl, useProxy).orElse(null),
            getAuthorizationEndpointFra(wellKnownUrl).orElse(null),
            !ENV.isLocal(), useProxy,
            ENV.getRequiredProperty(LOGINSERVICE_CLIENT_ID),
            false);
    }

    private static OidcProvider createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration(OidcProviderType.TOKENX,
            "oidc_tokenx",
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            getAuthorizationEndpointFra(wellKnownUrl).orElse(null),
            false, null,
            ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
            false);
    }

    private static OidcProvider createConfiguration(OidcProviderType type,
                                                    String providerName,
                                                    String issuer,
                                                    String jwks,
                                                    URI tokenEndpoint,
                                                    URI authorizationEndpoint,
                                                    boolean useProxyForJwks,
                                                    String proxy,
                                                    String clientName,
                                                    boolean skipAudienceValidation) {
        return new OidcProvider(
                        type,
                        url(issuer, "issuer", providerName),
                        url(jwks, "jwks", providerName),
                        tokenEndpoint,
                        authorizationEndpoint,
                        useProxyForJwks,
                        proxy,
                        Objects.requireNonNull(clientName),
                    30,
                        skipAudienceValidation);
    }

    private static String getDefaultProxy() {
        return ENV.getProperty(PROXY_KEY, DEFAULT_PROXY_URL);
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
