package no.nav.vedtak.server.localdev;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Felles oppsett for JettyDevServer i IDE. Ikke bruk utenom JettyDevServer.
 */
public final class LocalDevProperties {


    private static final Environment ENV = Environment.current();

    private LocalDevProperties() {
    }

    /**
     * Konfigurerer SSL truststore/keystore og Kafka-credentials for lokal utvikling med JettyDevServer i IDE
     * Leser properties: keystore.relativ.path, truststore.relativ.path, vtp.ssl.passord, user.home.
     */
    public static void setPropertiesForLocalDev() {
        if (!ENV.isLocal()) {
            throw new IllegalStateException("LocalDevProperties skal kun brukes i lokal utvikling");
        }
        var keystoreRelativPath = ENV.getProperty("keystore.relativ.path");
        var truststoreRelativPath = ENV.getProperty("truststore.relativ.path");
        var keystoreTruststorePassword = ENV.getProperty("vtp.ssl.passord");
        var absolutePathHome = ENV.getProperty("user.home", ".");
        System.setProperty("javax.net.ssl.trustStore", absolutePathHome + truststoreRelativPath);
        System.setProperty("javax.net.ssl.keyStore", absolutePathHome + keystoreRelativPath);
        System.setProperty("javax.net.ssl.trustStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.keyStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.password", keystoreTruststorePassword);
        System.setProperty("KAFKA_TRUSTSTORE_PATH", absolutePathHome + truststoreRelativPath);
        System.setProperty("KAFKA_KEYSTORE_PATH", absolutePathHome + keystoreRelativPath);
        System.setProperty("KAFKA_CREDSTORE_PASSWORD", keystoreTruststorePassword);
    }

}
