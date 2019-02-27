package no.nav.vedtak.felles.integrasjon.kafka;

public interface AksjonspunktKafkaProducer {
    void publiserEvent(String key, String behandlingProsessEventJson);
}
