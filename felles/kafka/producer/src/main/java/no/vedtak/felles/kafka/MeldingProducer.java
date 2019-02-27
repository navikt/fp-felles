package no.vedtak.felles.kafka;

public interface MeldingProducer {

    void sendJson(String json);

    void sendJsonMedNøkkel(String nøkkel, String json);

    void flushAndClose();

}
