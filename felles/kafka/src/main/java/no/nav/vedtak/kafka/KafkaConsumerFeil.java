package no.nav.vedtak.kafka;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KafkaConsumerFeil extends DeklarerteFeil {

    KafkaConsumerFeil FACTORY = FeilFactory.create(KafkaConsumerFeil.class);

    @TekniskFeil(feilkode = "FP-217605", feilmelding = "Klarte ikke parse input fra topic <%s>, payload = < %s >", logLevel = WARN)
    Feil klarteIkkeParseInput(String topic, String input);
}
