package no.nav.vedtak.felles.integrasjon.kafka;

import no.vedtak.felles.kafka.AksjonspunktMeldingProducer;
import no.vedtak.felles.kafka.MeldingProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AksjonspunktAksjonspunktKafkaProducerImpl implements AksjonspunktKafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(AksjonspunktAksjonspunktKafkaProducerImpl.class);
    private final MeldingProducer meldingProducer;

    @Inject
    AksjonspunktAksjonspunktKafkaProducerImpl(AksjonspunktMeldingProducer meldingProducer) {
        this.meldingProducer = meldingProducer;
    }

    public void publiserEvent(String key, String hendelseJson) {
        try{
            meldingProducer.sendJsonMedNøkkel(key,hendelseJson);
        }catch (Exception e) {
            logger.warn("Publisering av nytt event feilet ( : Json={}", hendelseJson, e);
        }
    }
}
