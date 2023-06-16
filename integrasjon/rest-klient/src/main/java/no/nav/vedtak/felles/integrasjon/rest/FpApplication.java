package no.nav.vedtak.felles.integrasjon.rest;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.Namespace;

public enum FpApplication {
    FPSAK,
    FPABAKUS,
    FPFORMIDLING,
    FPRISK,
    FPABONNENT,
    FPFORDEL,
    FPLOS,
    FPOPPDRAG,
    FPTILBAKE,
    FPDOKGEN,
    FPWSPROXY,
    FPOVERSIKT,
    NONFP;

    private static final Environment ENV = Environment.current();
    private static final Cluster CLUSTER = ENV.getCluster();
    // FpApplication brukes til å kalle apps i namespace foreldrepenger - ikke riktig å bruke ENV/namespace
    private static final Namespace FORELDREPENGER = Namespace.foreldrepenger();

    /*
     * Utelatt fpabonnent:8065
     */
    private static final Map<FpApplication, Integer> LOCAL_PORTS = Map.ofEntries(
        Map.entry(FpApplication.FPSAK, 8080),
        Map.entry(FpApplication.FPABAKUS, 8015),
        Map.entry(FpApplication.FPFORMIDLING, 8010),
        Map.entry(FpApplication.FPRISK, 8075),
        Map.entry(FpApplication.FPOPPDRAG, 8070),
        Map.entry(FpApplication.FPTILBAKE, 8030),
        Map.entry(FpApplication.FPFORDEL, 8090),
        Map.entry(FpApplication.FPDOKGEN, 8291),
        Map.entry(FpApplication.FPWSPROXY, 8292),
        Map.entry(FpApplication.FPLOS, 8071),
        Map.entry(FpApplication.FPOVERSIKT, 8889)
    );

    private static final Set<FpApplication> GCP_APPS = Set.of(FPOVERSIKT);

    public boolean specified() {
        return !NONFP.equals(this);
    }

    public static String contextPathFor(FpApplication application) {
        if (application == null || NONFP.equals(application)) {
            throw new IllegalArgumentException("Utviklerfeil: angitt app er ikke i fp-familien");
        }
        var appname = application.name().toLowerCase();
        // Sjekk om override for kjøring i IDE <app>.override.url=http://localhost:localport/<appname> (evt med port og annen path)
        var override = contextPathProperty(application);
        if (CLUSTER.isLocal() && override!= null) {
            return override;
        }
        // Sjekk om kryss-lokasjon - da trengs ingress og litt ulike varianter
        var clusterForApplication = getCluster(application);
        if (!CLUSTER.equals(clusterForApplication)) {
            var prefix = "https://" + appname;
            if (ENV.isFss()) { // Kaller fra FSS til GCP
                return prefix + ".intern" + (ENV.isProd() ? "" : ".dev") + ".nav.no/" + appname;
            } else if (ENV.isGcp()) { // Kaller fra GCP til FSS
                if (FPSAK.equals(application)) {
                    return prefix + "-api." + clusterForApplication.clusterName() + "-pub.nais.io/" + appname;
                }
                return prefix + "." + clusterForApplication.clusterName() + "-pub.nais.io/" + appname;
            } else {
                throw new IllegalStateException("Utviklerfeil: Skal ikke komme hit");
            }
        }
        // Samme lokasjon og cluster - bruk service discovery
        var prefix = "http://" + appname;
        return switch (CLUSTER) {
            case DEV_FSS, PROD_FSS -> prefix + "/" + appname;
            case VTP -> prefix + ":8080/" + appname;
            default -> throw new IllegalArgumentException("Ikke implementert for Cluster " + CLUSTER.clusterName());
        };
    }

    public static String scopesFor(FpApplication application) {
        if (CLUSTER.isLocal()) {
            return "api://" + Cluster.VTP.clusterName() + "." + FORELDREPENGER.getName() + "." + Cluster.VTP.clusterName() + "/.default";
        }
        return "api://" + getCluster(application).clusterName() + "." + FORELDREPENGER.getName() + "." + application.name().toLowerCase() + "/.default";
    }

    private static String contextPathProperty(FpApplication application) {
        return Optional.ofNullable(ENV.getProperty(application.name().toLowerCase() + ".override.url"))
            .map(s -> s.replace("localhost:localport", "localhost:" + LOCAL_PORTS.get(application)))
            .orElse(null);
    }

    private static Cluster getCluster(FpApplication application) {
        if (CLUSTER.isProd()) {
            return GCP_APPS.contains(application) ? Cluster.PROD_GCP : Cluster.PROD_FSS;
        } else if (CLUSTER.isDev()) {
            return GCP_APPS.contains(application) ? Cluster.DEV_GCP : Cluster.DEV_FSS;
        } else {
            return Cluster.VTP;
        }
    }

}
