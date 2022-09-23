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
    NONFP
    ;

    private static final Environment ENV = Environment.current();
    private static final Cluster CLUSTER  = ENV.getCluster();
    private static final Namespace NAMESPACE = ENV.getNamespace();

    /*
     * Utelatt fpabonnent:8065, fpinfo:8040
     */
    private static final Map<FpApplication, Integer> LOCAL_PORTS = Map.of(
        FpApplication.FPSAK, 8080,
        FpApplication.FPABAKUS, 8015,
        FpApplication.FPFORMIDLING, 8010,
        FpApplication.FPRISK, 8075,
        FpApplication.FPOPPDRAG, 8070,
        FpApplication.FPTILBAKE, 8030,
        FpApplication.FPFORDEL, 8090,
        FpApplication.FPDOKGEN, 8291,
        FpApplication.FPLOS, 8071,
        FpApplication.FPINFO, 8040
    );

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
        return "api://" + CLUSTER.clusterName() + "." + NAMESPACE.getName() + "." + application.name().toLowerCase() + "/.default";
    }

    private String contextPathProperty() {
        return this.name() + ".override.url";
    }
}
