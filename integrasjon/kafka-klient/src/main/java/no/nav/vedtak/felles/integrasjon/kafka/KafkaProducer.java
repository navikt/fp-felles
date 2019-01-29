package no.nav.vedtak.felles.integrasjon.kafka;

public interface KafkaProducer {
    void publiserEvent(Long key, String behandlingProsessEventJson);
}