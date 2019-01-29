package no.vedtak.felles.kafka;

public interface MeldingProducer {

    void sendOppgaveMedJson(Long behandlingId,String json);

    void flushAndClose();

}
