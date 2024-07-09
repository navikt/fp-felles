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
import no.nav.vedtak.sikkerhet.kontekst.Systembruker;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.config.TokenXProperty;

public final class OidcProviderConfig {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(OidcProviderConfig.class);

    private static final String STS_WELL_KNOWN_URL = "oidc.sts.well.known.url";
    private static final String STS_CONFIG_ISSUER = "oidc.sts.openid.config.issuer";
    private static final String STS_CONFIG_JWKS_URI = "oidc.sts.openid.config.jwks.uri";
    private static final String STS_CONFIG_TOKEN_ENDPOINT = "oidc.sts.openid.config.token.endpoint";

    private static final String AZURE_HTTP_PROXY = "azure.http.proxy"; // settes ikke av naiserator

    private static final String PROXY_KEY = "proxy.url"; // FP-oppsett lite brukt
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";

    private static Set<OpenIDConfiguration> providers = new HashSet<>();

    private final Set<OpenIDConfiguration> instanceProviders;
    private final Map<String, OpenIDConfiguration> issuers;

    private static OidcProviderConfig instance;

    private OidcProviderConfig() {
        this(init());
    }

    private OidcProviderConfig(Set<OpenIDConfiguration> providers) {
        this.instanceProviders = new HashSet<>(providers);
        this.issuers = this.instanceProviders.stream().collect(Collectors.toMap(c -> c.issuer().toString(), Function.identity()));
    }

    public static synchronized OidcProviderConfig instance() {
        var inst = instance;
        if (inst == null) {
            inst = new OidcProviderConfig();
            instance = inst;
        }
        return inst;
    }

    public Optional<OpenIDConfiguration> getOidcConfig(OpenIDProvider type) {
        return instanceProviders.stream().filter(p -> p.type().equals(type)).findFirst();
    }

    public Optional<OpenIDConfiguration> getOidcConfig(String issuer) {
        return Optional.ofNullable(issuers.get(issuer));
    }

    private static synchronized Set<OpenIDConfiguration> init() {
        if (providers.isEmpty()) {
            var configs = hentConfig();
            providers = configs;
        }
        return providers;
    }

    private static Set<OpenIDConfiguration> hentConfig() {
        Set<OpenIDConfiguration> idProviderConfigs = new HashSet<>();

        // OIDC STS
        if (ENV.getProperty(STS_WELL_KNOWN_URL) != null
            || ENV.getProperty(STS_CONFIG_ISSUER) != null) { // Det er kanskje noen apper som ikke bruker STS token validering??
            idProviderConfigs.add(createStsConfiguration(ENV.getProperty(STS_WELL_KNOWN_URL)));
        }

        // Azure - ikke alle apps trenger denne (tokenx-apps)
        var azureKonfigUrl = getAzureProperty(AzureProperty.AZURE_APP_WELL_KNOWN_URL);
        if (azureKonfigUrl != null) {
            LOG.debug("Oppretter AzureAD konfig fra '{}'", azureKonfigUrl);
            idProviderConfigs.add(createAzureAppConfiguration(azureKonfigUrl));
        }

        // TokenX
        var tokenxKonfigUrl = getTokenXProperty(TokenXProperty.TOKEN_X_WELL_KNOWN_URL);
        if (tokenxKonfigUrl != null) {
            LOG.debug("Oppretter TokenX konfig fra '{}'", tokenxKonfigUrl);
            idProviderConfigs.add(createTokenXConfiguration(tokenxKonfigUrl));
        }

        var providere = idProviderConfigs.stream().map(OpenIDConfiguration::type).map(OpenIDProvider::name).collect(Collectors.joining(", "));
        LOG.info("ID Providere som er tilgjengelig: {}", providere);

        return idProviderConfigs;
    }

    private static OpenIDConfiguration createStsConfiguration(String wellKnownUrl) {
        return createConfiguration(OpenIDProvider.STS,
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_ISSUER)).or(() -> getIssuerFra(wellKnownUrl)).orElse(null),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_JWKS_URI)).or(() -> getJwksFra(wellKnownUrl)).orElse(null),
            Optional.ofNullable(ENV.getProperty(STS_CONFIG_TOKEN_ENDPOINT)).or(() -> getTokenEndpointFra(wellKnownUrl)).orElse(null),
            false, null,
            Systembruker.username(),
            Systembruker.password(),
            true);
    }

    @SuppressWarnings("unused")
    private static OpenIDConfiguration createAzureAppConfiguration(String wellKnownUrl) {
        var proxyUrl = ENV.isFss() ? URI.create(ENV.getProperty(AZURE_HTTP_PROXY, getDefaultProxy())) : null;
        return createConfiguration(OpenIDProvider.AZUREAD,
            Optional.ofNullable(getAzureProperty(AzureProperty.AZURE_OPENID_CONFIG_ISSUER))
                .orElseGet(() -> getIssuerFra(wellKnownUrl, proxyUrl).orElse(null)),
            Optional.ofNullable(getAzureProperty(AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI))
                .orElseGet(() -> getJwksFra(wellKnownUrl, proxyUrl).orElse(null)),
            Optional.ofNullable(getAzureProperty(AzureProperty.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT))
                .orElseGet(() -> getTokenEndpointFra(wellKnownUrl, proxyUrl).orElse(null)),
            ENV.isFss(),
            proxyUrl,
            getAzureProperty(AzureProperty.AZURE_APP_CLIENT_ID),
            getAzureProperty(AzureProperty.AZURE_APP_CLIENT_SECRET),
            ENV.isLocal());
    }

    private static String getAzureProperty(AzureProperty property) {
        return Optional.ofNullable(ENV.getProperty(property.name()))
            .orElseGet(() -> ENV.getProperty(property.name().toLowerCase().replace('_', '.')));
    }

    private static String getTokenXProperty(TokenXProperty property) {
        return Optional.ofNullable(ENV.getProperty(property.name()))
            .orElseGet(() -> ENV.getProperty(property.name().toLowerCase().replace('_', '.')));
    }

    private static OpenIDConfiguration createTokenXConfiguration(String wellKnownUrl) {
        return createConfiguration(OpenIDProvider.TOKENX, getIssuerFra(wellKnownUrl).orElseThrow(), getJwksFra(wellKnownUrl).orElseThrow(),
            getTokenEndpointFra(wellKnownUrl).orElse(null), false, null, getTokenXProperty(TokenXProperty.TOKEN_X_CLIENT_ID), null,
            // Signerer requests med jws
            false);
    }

    private static OpenIDConfiguration createConfiguration(OpenIDProvider type,
                                                           // NOSONAR
                                                           String issuer,
                                                           String jwks,
                                                           String tokenEndpoint,
                                                           boolean useProxyForJwks,
                                                           URI proxy,
                                                           String clientName,
                                                           String clientPassword,
                                                           boolean skipAudienceValidation) {
        return new OpenIDConfiguration(type, tilURI(issuer, "issuer", type), tilURI(jwks, "jwksUri", type),
            tokenEndpoint != null ? tilURI(tokenEndpoint, "tokenEndpoint", type) : null, useProxyForJwks, proxy, Objects.requireNonNull(clientName),
            clientPassword, skipAudienceValidation);
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
