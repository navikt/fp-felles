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
    FPINFO,
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
        Map.entry(FpApplication.FPINFO, 8040),
        Map.entry(FpApplication.FPOVERSIKT, 8020)
    );

    private static final Set<FpApplication> GCP_APPS = Set.of(FPOVERSIKT);

    public boolean specified() {
        return !NONFP.equals(this);
    }


    public static String contextPathFor(FpApplication application) {
        return contextPathFor(application, ENV.getCluster());
    }

    public static String scopesFor(FpApplication application) {
        return scopesFor(application, ENV.getCluster());
    }

    static String contextPathFor(FpApplication application, Cluster clusten) {
        if (application == null || NONFP.equals(application)) {
            throw new IllegalArgumentException("Utviklerfeil: angitt app er ikke i fp-familien");
        }
        var appname = application.name().toLowerCase();

        if (clusten.isLocal()) {
            return Optional.ofNullable(ENV.getProperty(application.name().toLowerCase() + ".override.url"))
                .orElseGet(() -> String.format("http://localhost:%s/%s", LOCAL_PORTS.get(application), appname));
        }

        var clusterForApplication = getClusterTilFPApplikasjonenSomSkalKalles(application, clusten);
        if (clusten.isCoLocated(clusterForApplication)) {
            return String.format("http://%s/%s", appname, appname);
        } else {
            var prefix = "https://" + appname;
            if (clusten.isFss()) { // Kaller fra FSS til GCP
                return prefix + ".intern" + (clusten.isProd() ? "" : ".dev") + ".nav.no/" + appname;
            } else { // Kaller fra GCP til FSS
                if (FPSAK.equals(application)) {
                    prefix += "-api";
                }
                return prefix + "." + clusterForApplication.clusterName() + "-pub.nais.io/" + appname;
            }
        }
    }

    static String scopesFor(FpApplication application, Cluster cluster) {
        if (cluster.isLocal()) {
            return "api://" + Cluster.VTP.clusterName() + "." + FORELDREPENGER.getName() + "." + Cluster.VTP.clusterName() + "/.default";
        }
        return "api://" + getClusterTilFPApplikasjonenSomSkalKalles(application, cluster).clusterName() + "." + FORELDREPENGER.getName() + "." + application.name().toLowerCase() + "/.default";
    }

    private static Cluster getClusterTilFPApplikasjonenSomSkalKalles(FpApplication application, Cluster cluster) {
        if (cluster.isProd()) {
            return GCP_APPS.contains(application) ? Cluster.PROD_GCP : Cluster.PROD_FSS;
        }
        if (cluster.isDev()) {
            return GCP_APPS.contains(application) ? Cluster.DEV_GCP : Cluster.DEV_FSS;
        }
        throw new IllegalArgumentException("Utviklerfeil: Skal ikke kunne nå her med cluster annet enn de som er definert over");
    }
}
