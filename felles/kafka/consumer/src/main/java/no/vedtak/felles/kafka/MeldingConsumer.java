package no.vedtak.felles.kafka;

import java.util.List;

public interface MeldingConsumer {
    List<String> hentConsumerMeldingene();

    void manualCommitSync();

    void manualCommitAsync();

    List<String> hentConsumerMeldingeneFraStarten();

    void close();
}
