package no.nav.vedtak.felles.integrasjon.kafka;

/**
 * Standard navn på environment injisert av NAIS når kafka er enabled
 * Med vilje ikke public slik at man heller går via properties
 */
enum AivenProperty {
    KAFKA_BROKERS,
    KAFKA_CREDSTORE_PASSWORD,
    KAFKA_KEYSTORE_PATH,
    KAFKA_TRUSTSTORE_PATH,
    KAFKA_SCHEMA_REGISTRY,
    KAFKA_SCHEMA_REGISTRY_USER,
    KAFKA_SCHEMA_REGISTRY_PASSWORD
    ;

}
