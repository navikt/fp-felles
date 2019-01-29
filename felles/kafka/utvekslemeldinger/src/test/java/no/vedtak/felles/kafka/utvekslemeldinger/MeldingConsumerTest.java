package no.vedtak.felles.kafka.utvekslemeldinger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import no.vedtak.felles.kafka.MeldingConsumer;
import no.vedtak.felles.kafka.utvekslemeldinger.util.MessagesHelper;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import no.vedtak.felles.kafka.MeldingConsumerImpl;
import no.vedtak.felles.kafka.MeldingProducerImpl;


public class MeldingConsumerTest {

    private static final String TOPIC_NAME_MESSAGES = "messages";
    private static final String TOPIC_NAME_SINGLE_MESSAGES = "messages_single";
    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, 1, TOPIC_NAME_MESSAGES,TOPIC_NAME_SINGLE_MESSAGES);

    @Before
    public void before(){
        MessagesHelper.clearMessages();
    }

    @Test
    public void getMessagesWithOneMessageAtATime() {
        MessagesHelper.lagMeldingMedBehandlinsfrist();

        MeldingProducerImpl meldingProducerImpl = createSimpleProducer(TOPIC_NAME_MESSAGES);
        MessagesHelper.jsonMeldinger.forEach((key, value) -> {
            meldingProducerImpl.sendOppgaveMedJson(key, value);
        });

        meldingProducerImpl.flush();

        MeldingConsumerImpl meldingConsumer = createSimpleConsumer();
        List<String> messagesFromKafka = new ArrayList<>();

        hentMeldingene(meldingConsumer, messagesFromKafka);
        meldingConsumer.manualCommitSync();

        assertThat(messagesFromKafka).hasSize(1);

        hentMeldingene(meldingConsumer, messagesFromKafka);
        meldingConsumer.manualCommitSync();

        assertThat(messagesFromKafka).hasSize(2);
        assertThat(messagesFromKafka).containsAll(MessagesHelper.jsonMeldinger.values());
    }

    private void hentMeldingene(MeldingConsumer meldingConsumer, List<String> messagesFromkafka){
        List<String> response = meldingConsumer.hentConsumerMeldingene();
        messagesFromkafka.addAll(response);
    }

    private MeldingProducerImpl createSimpleProducer(String topic) {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
        return new MeldingProducerImpl(topic,
                senderProps.get("bootstrap.servers").toString(),
                "https://kafka-test-schema-registry.nais.preprod.local",
                ""
                ,""
                ,"");
    }

    private MeldingConsumerImpl createSimpleConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("sampleRawConsumer1", "false", embeddedKafka);
        return new MeldingConsumerImpl(TOPIC_NAME_MESSAGES,
                consumerProps.get("bootstrap.servers").toString(),
                "https://kafka-test-schema-registry.nais.preprod.local",
                consumerProps.get("group.id").toString(),
                "",
                "");
    }
}
