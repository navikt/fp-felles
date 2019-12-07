package no.nav.vedtak.util.env;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import no.nav.vedtak.konfig.EnvPropertiesKonfigVerdiProvider;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.KonfigVerdi.Converter;
import no.nav.vedtak.konfig.KonfigVerdiProvider;
import no.nav.vedtak.konfig.PropertyFileKonfigProvider;
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
                new PropertyFileKonfigProvider());
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

    public String namespace() {
        return namespace.getNamespace();
    }

    public String getProperty(String key) {
        return getProperty(key, String.class, null);
    }

    public String getProperty(String key, String defaultVerdi) {
        return getProperty(key, String.class, defaultVerdi);
    }

    public <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultVerdi) {
        var converter = converterFor(targetType);
        if (converter == null && !targetType.equals(String.class)) {
            throw new IllegalArgumentException("Konvertering til " + targetType + " er ikke støttet");
        }
        for (var source : propertySources) {
            if (source.harVerdi(key)) {
                Object verdi = source.getVerdi(key, converter);
                if (verdi != null) {
                    return (T) verdi;
                }
            }
        }
        return defaultVerdi;
    }

    private <T> Converter<?> converterFor(Class<T> targetType) {
        try {
            if (targetType.equals(Period.class)) {
                return KonfigVerdi.PeriodConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(Duration.class)) {
                return KonfigVerdi.DurationConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(LocalDate.class)) {
                return KonfigVerdi.LocalDateConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(Long.class)) {
                return KonfigVerdi.LongConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
                return KonfigVerdi.BooleanConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(URI.class)) {
                return KonfigVerdi.UriConverter.class.getDeclaredConstructor().newInstance();
            }
            if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
                return KonfigVerdi.IntegerConverter.class.getDeclaredConstructor().newInstance();
            }
            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Uventet feil ved konstruksjon av konverter");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[cluster=" + cluster + ", namespace=" + namespace + ", propertySources="
                + propertySources + "]";
    }

}
