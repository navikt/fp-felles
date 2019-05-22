package no.nav.vedtak.felles.integrasjon.jms;

import javax.inject.Named;

/**
 * Definerer konfig for en JMS meldingskø.
 * Bruke {@link Named} for å identifisere en gitt queue konfigurasjon.
 */
public interface JmsKonfig {

    String getQueueManagerChannelName();

    String getQueueManagerHostname();

    String getQueueManagerName();

    int getQueueManagerPort();

    String getQueueManagerUsername();

    String getQueueName();

    boolean harReplyToQueue();

    String getReplyToQueueName();



}
