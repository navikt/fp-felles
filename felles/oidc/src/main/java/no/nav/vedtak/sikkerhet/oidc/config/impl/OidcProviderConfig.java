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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.klient.http.ProxyProperty;
import no.nav.vedtak.sikkerhet.kontekst.Systembruker;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.config.TokenXProperty;

public final class OidcProviderConfig {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(OidcProviderConfig.class);

    private static final String STS_WELL_KNOWN_URL = "oidc.sts.well.known.url";
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
        if (ENV.getProperty(STS_WELL_KNOWN_URL) != null) { // Det er kanskje noen apper som ikke bruker STS token validering??
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
            getIssuerFra(wellKnownUrl).orElse(null),
            getJwksFra(wellKnownUrl).orElse(null),
            getTokenEndpointFra(wellKnownUrl).orElse(null),
            false,
            null,
            Systembruker.username(),
            Systembruker.password(),
            true);
    }

    @SuppressWarnings("unused")
    private static OpenIDConfiguration createAzureAppConfiguration(String azureKonfigUrl) {
        var proxyUrl = (ENV.isFss() && ENV.isProd()) ? ProxyProperty.getProxy() : null;
        return createConfiguration(OpenIDProvider.AZUREAD,
            getPropertyOrWellKnown(AzureProperty.AZURE_OPENID_CONFIG_ISSUER.name(), () -> getIssuerFra(azureKonfigUrl, proxyUrl)),
            getPropertyOrWellKnown(AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI.name(), () -> getJwksFra(azureKonfigUrl, proxyUrl)),
            getPropertyOrWellKnown(AzureProperty.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT.name(), () -> getTokenEndpointFra(azureKonfigUrl, proxyUrl)),
            (ENV.isFss() && ENV.isProd()),
            proxyUrl,
            getAzureProperty(AzureProperty.AZURE_APP_CLIENT_ID),
            getAzureProperty(AzureProperty.AZURE_APP_CLIENT_SECRET),
            ENV.isLocal());
    }

    private static OpenIDConfiguration createTokenXConfiguration(String tokenxKonfigUrl) {
        return createConfiguration(OpenIDProvider.TOKENX,
            getPropertyOrWellKnown(TokenXProperty.TOKEN_X_ISSUER.name(), () -> getIssuerFra(tokenxKonfigUrl)),
            getPropertyOrWellKnown(TokenXProperty.TOKEN_X_JWKS_URI.name(), () -> getJwksFra(tokenxKonfigUrl)),
            getPropertyOrWellKnown(TokenXProperty.TOKEN_X_TOKEN_ENDPOINT.name(), () -> getTokenEndpointFra(tokenxKonfigUrl)),
            false,
            null,
            getTokenXProperty(TokenXProperty.TOKEN_X_CLIENT_ID),
            null,
            // Signerer requests med jws
            false);
    }

    private static String getPropertyOrWellKnown(String propertyname, Supplier<Optional<String>> wellknownSupplier) {
        return getProperty(propertyname)
            .or(wellknownSupplier)
            .orElse(null);
    }

    private static Optional<String> getProperty(String propertyname) {
        return Optional.ofNullable(ENV.getProperty(propertyname))
            .or(() -> Optional.ofNullable(ENV.getProperty(propertyname.toLowerCase().replace('_', '.'))));
    }

    private static String getAzureProperty(AzureProperty property) {
        return getProperty(property.name()).orElse(null);
    }

    private static String getTokenXProperty(TokenXProperty property) {
        return getProperty(property.name()).orElse(null);
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

    private static URI tilURI(String url, String key, OpenIDProvider provider) {
        try {
            return URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new TekniskException("F-644196",
                String.format("Syntaksfeil i token validator konfigurasjonen av '%s' for '%s'", key, provider.name()),
                e);
        }
    }
}
