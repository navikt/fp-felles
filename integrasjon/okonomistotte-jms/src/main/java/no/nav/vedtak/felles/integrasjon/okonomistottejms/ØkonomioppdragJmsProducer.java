package no.nav.vedtak.felles.integrasjon.okonomistottejms;

import no.nav.vedtak.felles.integrasjon.jms.ExternalQueueProducer;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;

public abstract class ØkonomioppdragJmsProducer extends ExternalQueueProducer {
    public ØkonomioppdragJmsProducer() {
    }

    public ØkonomioppdragJmsProducer(JmsKonfig konfig) {
        super(konfig);
    }

    /**
     * Legg oppdragXml på kø til oppdragssystemet.
     *
     * @param oppdragXML OppdragXml som representerer en oppdragsmottaker.
     */
    public abstract void sendØkonomiOppdrag(String oppdragXML);
}
