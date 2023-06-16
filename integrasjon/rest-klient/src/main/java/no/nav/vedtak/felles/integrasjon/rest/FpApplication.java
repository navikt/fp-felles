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
        return contextPathFor(application, ENV);
    }

    public static String scopesFor(FpApplication application) {
        return scopesFor(application, ENV);
    }

    static String contextPathFor(FpApplication application, Environment currentEnvironment) {
        if (application == null || NONFP.equals(application)) {
            throw new IllegalArgumentException("Utviklerfeil: angitt app er ikke i fp-familien");
        }
        var appname = application.name().toLowerCase();

        if (currentEnvironment.isLocal()) {
            return urlForLocal(application, currentEnvironment, appname);
        }

        var clusterForApplication = getClusterTilFPApplikasjonenSomSkalKalles(application, currentEnvironment);
        if (currentEnvironment.getCluster().isCoLocated(clusterForApplication)) {
            return String.format("http://%s/%s", appname, appname); // service discovery
        }

        return urlForCommunicationBetweenDifferentClusters(application, currentEnvironment, appname, clusterForApplication);

    }

    private static String urlForCommunicationBetweenDifferentClusters(FpApplication application, Environment currentEnvironment, String appname, Cluster clusterForApplication) {
        var prefix = "https://" + appname;
        if (currentEnvironment.isFss()) { // Kaller fra FSS til GCP
            return prefix + ".intern" + (currentEnvironment.isProd() ? "" : ".dev") + ".nav.no/" + appname;
        } else { // Kaller fra GCP til FSS
            if (FPSAK.equals(application)) {
                prefix += "-api";
            }
            return prefix + "." + clusterForApplication.clusterName() + "-pub.nais.io/" + appname;
        }
    }

    private static String urlForLocal(FpApplication application, Environment currentEnvironment, String appname) {
        return Optional.ofNullable(currentEnvironment.getProperty(application.name().toLowerCase() + ".override.url"))
            .orElseGet(() -> String.format("http://localhost:%s/%s", LOCAL_PORTS.get(application), appname));
    }

    static String scopesFor(FpApplication application, Environment currentEnvironment) {
        if (currentEnvironment.isLocal()) {
            return "api://" + Cluster.VTP.clusterName() + "." + FORELDREPENGER.getName() + "." + Cluster.VTP.clusterName() + "/.default";
        }
        return "api://" + getClusterTilFPApplikasjonenSomSkalKalles(application, currentEnvironment).clusterName() + "." + FORELDREPENGER.getName() + "." + application.name().toLowerCase() + "/.default";
    }

    private static Cluster getClusterTilFPApplikasjonenSomSkalKalles(FpApplication application, Environment currentEnvironment) {
        if (currentEnvironment.isProd()) {
            return GCP_APPS.contains(application) ? Cluster.PROD_GCP : Cluster.PROD_FSS;
        }
        if (currentEnvironment.isDev()) {
            return GCP_APPS.contains(application) ? Cluster.DEV_GCP : Cluster.DEV_FSS;
        }
        throw new IllegalArgumentException("Utviklerfeil: Skal ikke kunne nå her med cluster annet enn de som er definert over");
    }
}
