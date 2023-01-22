package no.nav.foreldrepenger.konfig;

import static java.lang.System.getenv;

public class ClientId {

    private static final String DELIMIT = ":";

    private final String clientId;

    private ClientId(String clientId) {
        this.clientId = clientId;
    }

    public static ClientId of(String clientId) {
        return new ClientId(clientId);
    }

    public static ClientId of(Cluster cluster, Namespace namespace, Application application) {
        return of(cluster.clusterName(), namespace.getName(), application.getName());
    }

    public static ClientId of(Cluster cluster, Namespace namespace, String application) {
        return of(cluster.clusterName(), namespace.getName(), application);
    }

    private static ClientId of(String cluster, String namespace, String application) {
        return of(cluster + DELIMIT + namespace + DELIMIT + application);
    }

    public String getClientId() {
        return clientId;
    }

    public static ClientId current() {
        return ClientId.of(getenv(NaisProperty.CLIENTID.propertyName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[clientId=" + clientId + "]";
    }

}
