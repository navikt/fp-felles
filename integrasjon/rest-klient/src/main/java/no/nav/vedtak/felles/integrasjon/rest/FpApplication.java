package no.nav.vedtak.felles.integrasjon.rest;

import java.util.Map;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;

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
    NONFP
    ;

    private static final Cluster CLUSTER = Environment.current().getCluster();
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
        FpApplication.FPLOS, 8071
    );

    public boolean specified() {
        return !NONFP.equals(this);
    }

    public static String contextPathFor(FpApplication application) {
        var prefix = "http://" + application.name().toLowerCase();
        return switch (CLUSTER) {
            case DEV_FSS, PROD_FSS -> prefix + "/" + application.name().toLowerCase();
            case VTP -> prefix + ":8080/" + application.name().toLowerCase();
            case LOCAL -> "http://localhost:" + LOCAL_PORTS.get(application) + "/" + application.name().toLowerCase();
            default -> throw new IllegalArgumentException("Ikke implementert for Cluster " + CLUSTER.clusterName());
        };
    }

    public static String scopesFor(@SuppressWarnings("unused") FpApplication application) {
        return null; // TODO Elaborer når azure innføres
    }
}
