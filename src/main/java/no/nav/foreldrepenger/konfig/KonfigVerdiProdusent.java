package no.nav.foreldrepenger.konfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi.Converter;
import no.nav.foreldrepenger.konfig.KonfigVerdiProviderOutput.ProviderOutput;

/* Producer av konfig verdier. Støtter pluggbart antall providere av konfigurasjonsverdier. */
@ApplicationScoped
public class KonfigVerdiProdusent {
    private static final DefaultValueKonfigProvider DEFAULTVALUEPROVIDER = new DefaultValueKonfigProvider();
    private static final Pattern SKJUL = Pattern.compile(".*(passw?ord|[k|c]redential).*"); //$NON-NLS-1$
    private static final Logger log = LoggerFactory.getLogger(KonfigVerdiProdusent.class);

    private Instance<KonfigVerdiProvider> providerBeans;

    private List<KonfigVerdiProvider> providers = new ArrayList<>();

    private Set<String> konfigVerdiReferanser = new ConcurrentSkipListSet<>();

    @SuppressWarnings("rawtypes")
    private Map<Class<? extends KonfigVerdi.Converter>, KonfigVerdi.Converter> converters = new ConcurrentHashMap<>();

    KonfigVerdiProdusent() {
        // for CDI proxy
    }

    @Inject
    public KonfigVerdiProdusent(@Any Instance<KonfigVerdiProvider> providerBeans) {
        this.providerBeans = providerBeans;
    }

    @KonfigVerdi
    @Produces
    public String getKonfigVerdiString(InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        return verdi == null ? null : String.valueOf(verdi);
    }

    @KonfigVerdi
    @Produces
    public Boolean getKonfigVerdiBoolean(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null; //NOSONAR
        }
        return verdi instanceof Boolean ? (Boolean) verdi : Boolean.parseBoolean((String) verdi);
    }

    @KonfigVerdi
    @Produces
    public Integer getKonfigVerdiInteger(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null;
        }
        return verdi instanceof Integer ? (Integer) verdi : Integer.valueOf((String) verdi);
    }

    @KonfigVerdi
    @Produces
    public Period getKonfigVerdiPeriod(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null;
        }
        return verdi instanceof Period ? (Period) verdi : Period.parse((String) verdi);
    }

    @KonfigVerdi
    @Produces
    public Duration getKonfigVerdiDuration(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null;
        }
        return verdi instanceof Duration ? (Duration) verdi : Duration.parse((String) verdi);
    }

    @KonfigVerdi
    @Produces
    public LocalDate getKonfigVerdiLocalDate(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null;
        }
        return verdi instanceof LocalDate ? (LocalDate) verdi : LocalDate.parse((String) verdi);
    }

    @KonfigVerdi
    @Produces
    public Long getKonfigVerdiLong(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        if (verdi == null) {
            return null;
        }
        return verdi instanceof Long ? (Long) verdi : Long.valueOf((String) verdi);
    }

    /*
     * Støtter kun URI, ikke URL. Bør unngå URL som konfig verdier pga kjente
     * problemer med hashcode/equals og ytelse etc.
     */
    @KonfigVerdi
    @Produces
    public URI getKonfigVerdiUri(final InjectionPoint ip) {
        Object verdi = getEnkelVerdi(ip);
        try {
            if (verdi == null) {
                return null;
            }
            return verdi instanceof URI ? (URI) verdi : new URI((String) verdi);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("KonfigVerdi [" + verdi + "] er ikke en java.net.URI", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /*
     * Returnerer Liste av verdier.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @KonfigVerdi
    @Produces
    public <V> List<V> getKonfigVerdiList(final InjectionPoint ip) {
        KonfigVerdi annotation = getAnnotation(ip);
        String key = annotation.value();
        Converter converter = getConverter(annotation.converter());
        return getVerdi(ip, annotation, KonfigVerdiProviderOutput.LIST, key, converter);
    }

    /*
     * Returnerer Liste av verdier.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @KonfigVerdi
    @Produces
    public <V> Map<String, V> getKonfigVerdiMap(final InjectionPoint ip) {
        KonfigVerdi annotation = getAnnotation(ip);
        String key = annotation.value();
        Converter converter = getConverter(annotation.converter());
        return getVerdi(ip, annotation, KonfigVerdiProviderOutput.MAP, key, converter);
    }

    @SuppressWarnings("unchecked")
    public Object getEnkelVerdi(final InjectionPoint ip) {
        KonfigVerdi annotation = getAnnotation(ip);
        return getVerdi(ip, annotation, KonfigVerdiProviderOutput.SIMPLE);
    }

    @SuppressWarnings({ "rawtypes" })
    protected <T> T getVerdi(InjectionPoint ip, KonfigVerdi annotation, ProviderOutput<T> outputFunction) {
        String key = annotation.value();
        Converter converter = getConverter(annotation.converter());
        return getVerdi(ip, annotation, outputFunction, key, converter);

    }

    @SuppressWarnings("rawtypes")
    public <T> T getVerdi(InjectionPoint ip, KonfigVerdi annotation, ProviderOutput<T> outputFunction, String key,
            Converter converter) {
        for (KonfigVerdiProvider kvp : providers) {
            try {
                if (kvp.harVerdi(key)) {
                    T output = outputFunction.getOutput(kvp, key, converter);
                    sporKonfigVerdier(ip, annotation, output);
                    return output;
                }
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        "Kunne ikke slå opp verdi for key [" + key + "] fra " + kvp.getClass().getName()
                                + "; InjectionPoint=" + ip,
                        e);
            }
        }
        String defaultVerdi = annotation.defaultVerdi();
        if (annotation.required() && defaultVerdi.isEmpty()) {
            throw new IllegalStateException(
                    "Mangler verdi for key(required): " + annotation.value() + "; InjectionPoint=" + ip); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            if (!defaultVerdi.isEmpty()) {
                T output = outputFunction.getOutput(DEFAULTVALUEPROVIDER, defaultVerdi, converter);
                sporKonfigVerdier(ip, annotation, output);
                return output;
            }
        }
        return null;
    }

    public <T> void sporKonfigVerdier(InjectionPoint ip, KonfigVerdi annot, T output) {

        Member member = ip.getMember();
        String name = Constructor.class.isAssignableFrom(member.getClass())
                ? member.getName()
                : member.getDeclaringClass().getName() + "#" + member.getName(); //$NON-NLS-1$
        if (!konfigVerdiReferanser.contains(name)) {
            String key = annot.value();
            Object val = SKJUL.matcher(key).matches()
                    ? "********* (skjult)"// $NON-NLS-1$
                    : output;
            konfigVerdiReferanser.add(name);
            log.info("{}: {}=\"{}\" @{}", KonfigVerdi.class.getSimpleName(), key, val, name); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("rawtypes")
    private KonfigVerdi.Converter getConverter(Class<? extends KonfigVerdi.Converter<?>> converterClass) {
        KonfigVerdi.Converter converter = converters.get(converterClass);
        if (converter == null) {
            try {
                converter = converterClass.getDeclaredConstructor().newInstance();
                converters.put(converterClass, converter);
            } catch (ReflectiveOperationException e) {
                throw new UnsupportedOperationException("Mangler no-arg constructor for klasse: " + converterClass, e); //$NON-NLS-1$
            }
        }
        return converter;

    }

    protected KonfigVerdi getAnnotation(final InjectionPoint ip) {
        Annotated annotert = ip.getAnnotated();

        if (annotert == null) {
            throw new IllegalArgumentException("Mangler annotation KonfigVerdi for InjectionPoint=" + ip); //$NON-NLS-1$
        }
        if (annotert.isAnnotationPresent(KonfigVerdi.class)) {
            KonfigVerdi annotation = annotert.getAnnotation(KonfigVerdi.class);
            if (!annotation.value().isEmpty()) {
                return annotation;
            }
        }
        throw new IllegalStateException("Mangler key. Kan ikke være tom eller null: " + ip.getMember()); //$NON-NLS-1$
    }

    @PostConstruct
    public void init() {
        List<KonfigVerdiProvider> alleProviders = new ArrayList<>();
        for (KonfigVerdiProvider kvp : providerBeans) {
            alleProviders.add(kvp);
        }
        Collections.sort(alleProviders, Comparator.comparingInt(KonfigVerdiProvider::getPrioritet));

        providers.clear();
        providers.addAll(alleProviders);
    }

}
