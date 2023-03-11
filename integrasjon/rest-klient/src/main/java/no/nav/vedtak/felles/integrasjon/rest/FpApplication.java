package no.nav.vedtak.felles.integrasjon.rest;

import java.util.Map;

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
    NONFP;

    private static final Environment ENV = Environment.current();
    private static final Cluster CLUSTER = ENV.getCluster();
    // FpApplication brukes til å kalle apps i namespace foreldrepenger - ikke riktig å bruke ENV/namespace
    private static final Namespace FORELDREPENGER = Namespace.foreldrepenger();

    /*
     * Utelatt fpabonnent:8065
     */
    private static final Map<FpApplication, Integer> LOCAL_PORTS = Map.ofEntries(Map.entry(FpApplication.FPSAK, 8080),
        Map.entry(FpApplication.FPABAKUS, 8015), Map.entry(FpApplication.FPFORMIDLING, 8010), Map.entry(FpApplication.FPRISK, 8075),
        Map.entry(FpApplication.FPOPPDRAG, 8070), Map.entry(FpApplication.FPTILBAKE, 8030), Map.entry(FpApplication.FPFORDEL, 8090),
        Map.entry(FpApplication.FPDOKGEN, 8291), Map.entry(FpApplication.FPWSPROXY, 8292), Map.entry(FpApplication.FPLOS, 8071),
        Map.entry(FpApplication.FPINFO, 8040));

    public boolean specified() {
        return !NONFP.equals(this);
    }

    public static String contextPathFor(FpApplication application) {
        if (CLUSTER.isLocal() && ENV.getProperty(application.contextPathProperty()) != null) {
            return ENV.getProperty(application.contextPathProperty());
        }
        var prefix = "http://" + application.name().toLowerCase();
        return switch (CLUSTER) {
            case DEV_FSS, PROD_FSS -> prefix + "/" + application.name().toLowerCase();
            case VTP -> prefix + ":8080/" + application.name().toLowerCase();
            case LOCAL -> "http://localhost:" + LOCAL_PORTS.get(application) + "/" + application.name().toLowerCase();
            default -> throw new IllegalArgumentException("Ikke implementert for Cluster " + CLUSTER.clusterName());
        };
    }

    public static String scopesFor(FpApplication application) {
        if (CLUSTER.isLocal()) {
            return "api://" + Cluster.VTP.clusterName() + "." + FORELDREPENGER.getName() + "." + application.name().toLowerCase() + "/.default";
        }
        return "api://" + CLUSTER.clusterName() + "." + FORELDREPENGER.getName() + "." + application.name().toLowerCase() + "/.default";
    }

    private String contextPathProperty() {
        return this.name() + ".override.url";
    }

}
