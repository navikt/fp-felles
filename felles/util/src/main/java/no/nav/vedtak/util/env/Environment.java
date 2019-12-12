package no.nav.vedtak.util.env;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.vedtak.konfig.EnvPropertiesKonfigVerdiProvider;
import no.nav.vedtak.konfig.KonfigVerdi.BooleanConverter;
import no.nav.vedtak.konfig.KonfigVerdi.Converter;
import no.nav.vedtak.konfig.KonfigVerdi.DurationConverter;
import no.nav.vedtak.konfig.KonfigVerdi.IntegerConverter;
import no.nav.vedtak.konfig.KonfigVerdi.LocalDateConverter;
import no.nav.vedtak.konfig.KonfigVerdi.LongConverter;
import no.nav.vedtak.konfig.KonfigVerdi.PeriodConverter;
import no.nav.vedtak.konfig.KonfigVerdi.UriConverter;
import no.nav.vedtak.konfig.KonfigVerdiProvider;
import no.nav.vedtak.konfig.ApplicationPropertiesKonfigProvider;
import no.nav.vedtak.konfig.StandardPropertySource;
import no.nav.vedtak.konfig.PropertySourceMetaData;
import no.nav.vedtak.konfig.SystemPropertiesKonfigVerdiProvider;

public final class Environment {

    private final Cluster cluster;
    private final Namespace namespace;
    private final List<KonfigVerdiProvider> propertySources;

    private Environment(Cluster cluster, Namespace namespace) {
        this.cluster = cluster;
        this.namespace = namespace;
        this.propertySources = List.of(
                new SystemPropertiesKonfigVerdiProvider(),
                new EnvPropertiesKonfigVerdiProvider(),
                new ApplicationPropertiesKonfigProvider());
    }

    private static Environment of(Cluster cluster, Namespace namespace) {
        return new Environment(cluster, namespace);
    }

    public static Environment current() {
        return of(Cluster.current(), Namespace.current());
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public boolean isProd() {
        return cluster.isProd();
    }

    public String clusterName() {
        return cluster.clusterName();
    }

    public PropertySourceMetaData getProperties(StandardPropertySource source) {
        return propertySources.stream()
                .filter(p -> p.getSource().equals(source))
                .findFirst().map(p -> p.getAllProperties())
                .orElseThrow();
    }

    public String namespace() {
        return namespace.getNamespace();
    }

    public String getProperty(String key) {
        return getProperty(key, (String) null);
    }

    public String getRequiredProperty(String key) {
        return Optional.ofNullable(getProperty(key))
                .orElseThrow(() -> ikkeFunnet(key));
    }

    public String getProperty(String key, String defaultVerdi) {
        return getProperty(key, String.class, defaultVerdi);
    }

    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        return Optional.ofNullable(getProperty(key, targetType))
                .orElseThrow(() -> ikkeFunnet(key));
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultVerdi) {
        var converter = converterFor(targetType);
        if (converter == null && !targetType.equals(String.class)) {
            throw new IllegalArgumentException("Konvertering til " + targetType + " er ikke støttet");
        }

        return propertySources.stream()
                .filter(s -> s.harVerdi(key))
                .map(s -> s.getVerdi(key, converter))
                .filter(Objects::nonNull)
                .findFirst()
                .map(v -> (T) v)
                .orElse(defaultVerdi);
    }

    private static <T> Converter<?> converterFor(Class<T> targetType) {
        try {
            if (targetType.equals(Period.class)) {
                return construct(PeriodConverter.class);
            }
            if (targetType.equals(Duration.class)) {
                return construct(DurationConverter.class);
            }
            if (targetType.equals(LocalDate.class)) {
                return construct(LocalDateConverter.class);
            }
            if (targetType.equals(Long.class)) {
                return construct(LongConverter.class);
            }
            if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return construct(BooleanConverter.class);
            }
            if (targetType.equals(URI.class)) {
                return construct(UriConverter.class);
            }
            if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return construct(IntegerConverter.class);
            }
            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Uventet feil ved konstruksjon av konverter for " + targetType);
        }
    }

    private static <T> Converter<T> construct(Class<? extends Converter<T>> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private static IllegalStateException ikkeFunnet(String key) {
        throw new IllegalStateException(key + " ble ikke funnet");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[cluster=" + cluster + ", namespace=" + namespace + ", propertySources="
                + propertySources + "]";
    }

}
