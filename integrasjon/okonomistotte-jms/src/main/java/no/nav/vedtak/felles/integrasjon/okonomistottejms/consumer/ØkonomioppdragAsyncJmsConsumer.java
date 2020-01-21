package no.nav.vedtak.felles.integrasjon.okonomistottejms.consumer;

import no.nav.vedtak.felles.integrasjon.jms.QueueSelftest;


/**
* @deprecated Flytt til fpoppdrag
*/
@Deprecated
public interface ØkonomioppdragAsyncJmsConsumer extends QueueSelftest {
    void handle(String message);
}
