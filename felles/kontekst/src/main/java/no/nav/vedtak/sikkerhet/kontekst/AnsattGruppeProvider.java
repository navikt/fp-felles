package no.nav.vedtak.sikkerhet.kontekst;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class AnsattGruppeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AnsattGruppeProvider.class);
    private static final Environment ENV = Environment.current();
    private static final String SUFFIX = ".properties";

    private static final Map<AnsattGruppe, String> PROPERTY_NAME = Map.of(
        AnsattGruppe.SAKSBEHANDLER, "gruppe.oid.saksbehandler",
        AnsattGruppe.BESLUTTER, "gruppe.oid.beslutter",
        AnsattGruppe.OVERSTYRER, "gruppe.oid.overstyrer",
        AnsattGruppe.VEILEDER, "gruppe.oid.veileder",
        AnsattGruppe.OPPGAVESTYRER, "gruppe.oid.oppgavestyrer",
        AnsattGruppe.DRIFTER, "gruppe.oid.drifter",
        AnsattGruppe.FORTROLIG, "gruppe.oid.fortrolig",
        AnsattGruppe.STRENGTFORTROLIG, "gruppe.oid.strengtfortrolig",
        AnsattGruppe.SKJERMET, "gruppe.oid.skjermet"
    );

    private static final Map<String, AnsattGruppe> PROPERTY_NAME_REVERSE = PROPERTY_NAME.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private static AnsattGruppeProvider INSTANCE;

    private final Map<AnsattGruppe, UUID> ansattGruppeOid = new LinkedHashMap<>();
    private final Map<UUID, AnsattGruppe> oidAnsattGruppe = new LinkedHashMap<>();


    private AnsattGruppeProvider() {
        this(init());
    }

    private AnsattGruppeProvider(Map<AnsattGruppe, UUID> gruppeOidMap) {
        this.ansattGruppeOid.putAll(gruppeOidMap);
        gruppeOidMap.forEach((k,v) -> oidAnsattGruppe.put(v, k));
    }

    public static synchronized AnsattGruppeProvider instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new AnsattGruppeProvider();
            INSTANCE = inst;
        }
        return INSTANCE;
    }

    public UUID getAnsattGruppeOid(AnsattGruppe gruppe) {
        return ansattGruppeOid.get(gruppe);
    }

    public AnsattGruppe getAnsattGruppeFra(String value) {
        return Optional.ofNullable(value).map(AnsattGruppeProvider::safeUuidFraString).map(this::getAnsattGruppeFra).orElse(null);
    }

    public Set<AnsattGruppe> getAnsattGrupperFraStrings(List<String> values) {
        return values.stream().map(this::getAnsattGruppeFra).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public AnsattGruppe getAnsattGruppeFra(UUID value) {
        return Optional.ofNullable(value).map(oidAnsattGruppe::get).orElse(null);
    }

    public Set<AnsattGruppe> getAnsattGrupperFra(List<UUID> values) {
        return values.stream().map(this::getAnsattGruppeFra).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    static String getPropertyNavn(AnsattGruppe ansattGruppe) {
        return PROPERTY_NAME.get(ansattGruppe);
    }


    private static Map<AnsattGruppe, UUID> init() {
        Map<AnsattGruppe, UUID> resultat = new LinkedHashMap<>();
        lesFraResource(getInfix(), new Properties())
            .forEach((k, v) -> resultat.put(PROPERTY_NAME_REVERSE.get(k.toString()), UUID.fromString(v.toString())));
        resultat.putAll(lesFraEnv());
        return resultat;
    }

    private static Map<AnsattGruppe, UUID> lesFraEnv() {
        Map<AnsattGruppe, UUID> resultat = new LinkedHashMap<>();
        Arrays.stream(AnsattGruppe.values())
            .forEach(ag -> Optional.ofNullable(PROPERTY_NAME.get(ag)).map(ENV::getProperty).map(AnsattGruppeProvider::safeUuidFraString)
                .ifPresent(e -> resultat.put(ag, e)));
        return resultat;
    }

    private static Properties lesFraResource(String infix, Properties p) {
        if (infix == null) {
            return p;
        }
        String navn = AnsattGruppeProvider.class.getSimpleName().toLowerCase() + infix + SUFFIX;
        try (var is = AnsattGruppeProvider.class.getResourceAsStream(navn)) {
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

    private static UUID safeUuidFraString(String string) {
        try {
            return UUID.fromString(string);
        } catch (Exception e) {
            return null;
        }
    }

}
