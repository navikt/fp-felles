package no.nav.vedtak.sikkerhet.oidc.config.impl;

import static no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper.getIssuerFra;
import static no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper.getJwksFra;
import static no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper.getTokenEndpointFra;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public final class OidcProviderConfig {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(OidcProviderConfig.class);

    @Deprecated(forRemoval = true) // Neste tre linjer
    public static final String OPEN_AM_WELL_KNOWN_URL = "oidc.open.am.well.known.url";
    public static final String OPEN_AM_CLIENT_ID = "oidc.open.am.client.id";
    public static final String OPEN_AM_CLIENT_SECRET = "oidc.open.am.client.secret";

    private static final String STS_WELL_KNOWN_URL = "oidc.sts.well.known.url";
    // Disse 3 er kandidater for rydding. Alle apps skal ha satt well-known
    private static final String STS_CONFIG_ISSUER = "oidc.sts.openid.config.issuer";
    private static final String STS_CONFIG_JWKS_URI = "oidc.sts.openid.config.jwks.uri";
    private static final String STS_CONFIG_TOKEN_ENDPOINT = "oidc.sts.openid.config.token.endpoint";

    public  static final String AZURE_WELL_KNOWN_URL = "azure.app.well.known.url"; // naiserator
    public  static final String AZURE_CONFIG_ISSUER = "azure.openid.config.issuer"; // naiserator
    public  static final String AZURE_CONFIG_JWKS_URI = "azure.openid.config.jwks.uri"; // naiserator
    private static final String AZURE_CONFIG_TOKEN_ENDPOINT = "azure.openid.config.token.endpoint"; // naiserator
    public  static final String AZURE_CLIENT_ID = "azure.app.client.id"; // naiserator
    private static final String AZURE_CLIENT_SECRET = "azure.app.client.secret"; // naiserator
    private static final String AZURE_HTTP_PROXY = "azure.http.proxy"; // settes ikke av naiserator

    private static final String TOKEN_X_WELL_KNOWN_URL = "token.x.well.known.url"; // naiserator
    private static final String TOKEN_X_CLIENT_ID = "token.x.client.id"; // naiserator

    private static final String PROXY_KEY = "proxy.url"; // FP-oppsett lite brukt
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";

    private static Set<OpenIDConfiguration> PROVIDERS = new HashSet<>();

    private final Set<OpenIDConfiguration> providers;
    private final Map<String, OpenIDConfiguration> issuers;

    private static OidcProviderConfig instance;

    private OidcProviderConfig() {
        this(init());
    }

    private OidcProviderConfig(Set<OpenIDConfiguration> providers) {
        this.providers = new HashSet<>(providers);
        this.issuers = this.providers.stream().collect(Collectors.toMap(c -> c.issuer().toString(), Function.identity()));
    }

    public static synchronized OidcProviderConfig instance() {
        var inst= instance;
        if (inst == null) {
            inst = new OidcProviderConfig();
            instance = inst;
        }
        return inst;
    }

    public Optional<OpenIDConfiguration> getOidcConfig(OpenIDProvider type) {
        return providers.stream().filter(p -> p.type().equals(type)).findFirst();
    }

    public Optional<OpenIDConfiguration> getOidcConfig(String issuer) {
        return Optional.ofNullable(issuers.get(issuer));
    }

    private static synchronized Set<OpenIDConfiguration> init() {
        if (PROVIDERS.isEmpty()) {
            var configs = hentConfig();
            PROVIDERS = configs;
        }
        return PROVIDERS;
    }

    private static Set<OpenIDConfiguration> hentConfig() {
        Set<OpenIDConfiguration> idProviderConfigs = new HashSet<>();

        // OpenAm - kun vtp/test
        if (ENV.getProperty(OPEN_AM_WELL_KNOWN_URL) != null) {
            idProviderConfigs.add(createOpenAmConfiguration(ENV.getProperty(OPEN_AM_WELL_KNOWN_URL)));
        }

        // OIDC STS
        if (ENV.getProperty(STS_WELL_KNOWN_URL) != null || ENV.getProperty(STS_CONFIG_ISSUER) != null) { // Det er kanskje noen apper som ikke bruker STS token validering??
            idProviderConfigs.add(createStsConfiguration(ENV.getProperty(STS_WELL_KNOWN_URL)));
        }

        // Azure - ikke alle apps trenger denne (tokenx-apps)
        var azureKonfigUrl = ENV.getProperty(AZURE_WELL_KNOWN_URL);
        if (azureKonfigUrl != null) {
            LOG.debug("Oppretter AzureAD konfig fra '{}'", azureKonfigUrl);
            idProviderConfigs.add(createAzureAppConfiguration(azureKonfigUrl));
        }

        // TokenX
        var tokenxKonfigUrl = ENV.getProperty(TOKEN_X_WELL_KNOWN_URL);
        if (tokenxKonfigUrl != null) {
            LOG.debug("Oppretter TokenX konfig fra '{}'", tokenxKonfigUrl);
            idProviderConfigs.add(createTokenXConfiguration(tokenxKonfigUrl));
        }

        LOG.info("ID Providere som er tilgjengelig: {}", idProviderConfigs.stream()
            .map(OpenIDConfiguration::type)
            .map(OpenIDProvider::name)
            .collect(Collectors.joining(", ")));

        return idProviderConfigs;
    }

    private static OpenIDConfiguration createOpenAmConfiguration(String wellKnownUrl) {
        return createConfiguration(OpenIDProvider.ISSO,
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElseThrow(),
            false, null,
            ENV.getProperty(OPEN_AM_CLIENT_ID),
            ENV.getProperty(OPEN_AM_CLIENT_SECRET),
            true);
    }

    private static OpenIDConfiguration createStsConfiguration(String wellKnownUrl) {
        return createConfiguration(OpenIDProvider.STS,
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_ISSUER))
                .or(() -> getIssuerFra(wellKnownUrl)).orElse(null),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_JWKS_URI))
                .or(() -> getJwksFra(wellKnownUrl)).orElse(null),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_TOKEN_ENDPOINT))
                .or(() -> getTokenEndpointFra(wellKnownUrl)).orElse(null),
            false, null,
            ENV.getProperty("systembruker.username"),
            ENV.getProperty("systembruker.password"),
            true);
    }

    @SuppressWarnings("unused")
    private static OpenIDConfiguration createAzureAppConfiguration(String wellKnownUrl) {
        var useProxy = ENV.isLocal() ? null : URI.create(ENV.getProperty(AZURE_HTTP_PROXY, getDefaultProxy()));
        return createConfiguration(OpenIDProvider.AZUREAD,
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_ISSUER))
                .orElseGet(() -> getIssuerFra(wellKnownUrl, useProxy).orElse(null)),
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_JWKS_URI))
                .orElseGet(() -> getJwksFra(wellKnownUrl, useProxy).orElse(null)),
            Optional.ofNullable(ENV.getProperty(AZURE_CONFIG_TOKEN_ENDPOINT))
                .orElseGet(() -> getTokenEndpointFra(wellKnownUrl, useProxy).orElse(null)),
            !ENV.isLocal(), useProxy,
            ENV.getRequiredProperty(AZURE_CLIENT_ID),
            ENV.getProperty(AZURE_CLIENT_SECRET),
            false);
    }

    private static OpenIDConfiguration createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration(OpenIDProvider.TOKENX,
            getIssuerFra(wellKnownUrl).orElseThrow(),
            getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            false, null,
            ENV.getRequiredProperty(TOKEN_X_CLIENT_ID),
            null, // Signerer requests med jws
            false);
    }

    private static OpenIDConfiguration createConfiguration(OpenIDProvider type,
                                                           String issuer,
                                                           String jwks,
                                                           String tokenEndpoint,
                                                           boolean useProxyForJwks,
                                                           URI proxy,
                                                           String clientName,
                                                           String clientPassword,
                                                           boolean skipAudienceValidation) {
        return new OpenIDConfiguration(type,
            tilURI(issuer, "issuer", type),
            tilURI(jwks, "jwksUri", type),
            tokenEndpoint != null ? tilURI(tokenEndpoint, "tokenEndpoint", type) : null,
            useProxyForJwks,
            proxy,
            Objects.requireNonNull(clientName),
            clientPassword,
            skipAudienceValidation);
    }

    private static String getDefaultProxy() {
        return ENV.getProperty(PROXY_KEY, DEFAULT_PROXY_URL);
    }

    private static URI tilURI(String url, String key, OpenIDProvider provider) {
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new TekniskException("F-644196",
                    String.format("Syntaksfeil i token validator konfigurasjonen av '%s' for '%s'", key, provider.name()), e);
        }
    }
}
