package no.nav.vedtak.felles.integrasjon.kafka;

import no.vedtak.felles.kafka.MeldingProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KafkaProducerImpl implements no.nav.vedtak.felles.integrasjon.kafka.KafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducerImpl.class);
    private final MeldingProducer meldingProducer;

    @Inject
    KafkaProducerImpl(MeldingProducer meldingProducer) {
        this.meldingProducer = meldingProducer;
    }

    public void publiserEvent(Long key, String behandlingProsessEventJson) {
        try{
            meldingProducer.sendOppgaveMedJson(key,behandlingProsessEventJson);
        }catch (Exception e) {
            logger.warn("Publisering av nytt event feilet ( : behandlingProsessEventJson={}", behandlingProsessEventJson, e);
        }
    }
}