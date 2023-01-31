package no.nav.vedtak.sikkerhet.kontekst;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Brukes enn så lenge ifm Abac, STS-tokens, OnPrem-Kafka og WS-kall. Utledning av identtype ved intern STS
 * På sikt vil vi bruke verdier fra Nais (appname, clientId)
 */
public class Systembruker {

    private static final Environment ENV = Environment.current();
    private static final String SYSTEMBRUKER_USERNAME = ENV.getProperty("systembruker.username");
    private static final String SYSTEMBRUKER_PASSWORD = ENV.getProperty("systembruker.password");

    private Systembruker() {
    }

    public static String username() {
        return SYSTEMBRUKER_USERNAME;
    }

    public static String password() {
        return SYSTEMBRUKER_PASSWORD;
    }
}
