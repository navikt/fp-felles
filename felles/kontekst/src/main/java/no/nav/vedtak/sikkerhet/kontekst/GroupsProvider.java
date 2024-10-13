package no.nav.vedtak.sikkerhet.kontekst;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class GroupsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GroupsProvider.class);
    private static final String SUFFIX = ".properties";

    private static GroupsProvider INSTANCE;

    private final Properties properties;
    private final Map<String, Groups> reverse;


    private GroupsProvider() {
        this(init());
    }

    private GroupsProvider(Properties properties) {
        this.properties = properties;
        this.reverse = new HashMap<>();
        properties.forEach((k,v) -> reverse.put((String) v, Groups.valueOf(((String) k).toUpperCase())));
    }

    public static synchronized GroupsProvider instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new GroupsProvider();
            INSTANCE = inst;
        }
        return INSTANCE;
    }

    public String getGroupValue(Groups group) {
        return (String) properties.get(group.name().toLowerCase());
    }

    public Groups getGroupFrom(String value) {
        return Optional.ofNullable(value).map(reverse::get).orElse(null);
    }

    public Set<Groups> getGroupsFrom(List<String> values) {
        return values.stream().map(reverse::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Properties init() {
        var c = new Properties();
        lesFra(getInfix(), new Properties())
            .forEach((k, v) -> c.put(k.toString().toLowerCase(), v.toString()));
        return c;
    }

    private static Properties lesFra(String infix, Properties p) {
        if (infix == null) {
            return p;
        }
        String navn = GroupsProvider.class.getSimpleName().toLowerCase() + infix + SUFFIX;
        try (var is = GroupsProvider.class.getResourceAsStream(navn)) {
            if (is != null) {
                LOG.info("Laster groups fra {}", navn);
                p.load(is);
                return p;
            }
        } catch (IOException e) {
            LOG.info("Propertyfil {} ikke lesbar", navn);
        }
        LOG.info("Propertyfil {} ikke funnet", navn);
        return p;
    }

    private static String getInfix() {
        var cluster = Environment.current().getCluster();
        if (cluster.isLocal()) {
            return "-vtp";
        }
        return cluster.isProd() ? "-prod" : "-dev";
    }

}
