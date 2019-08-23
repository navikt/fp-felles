package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.jms.JMSException;

import no.nav.melding.virksomhet.hendelse.behandling.status.v1.BehandlingAvsluttet;
import no.nav.melding.virksomhet.hendelse.behandling.status.v1.BehandlingOpprettet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Alternative
@Priority(1)
public class NoopSakOgBehandlingClient implements SakOgBehandlingClient {
    protected final Logger log = LoggerFactory.getLogger(NoopSakOgBehandlingClient.class);


    @Override
    public void sendBehandlingOpprettet(BehandlingOpprettet behandlingOpprettet) {
        log.info("Invoked: sendBehandlingOpprettet");
    }

    @Override
    public void sendBehandlingAvsluttet(BehandlingAvsluttet behandlingAvsluttet) {
        log.info("Invoked: sendBehandlingAvsluttet");
    }

    @Override
    public void testConnection() throws JMSException {
        log.info("Invoked: testConnection");
    }

    @Override
    public String getConnectionEndpoint() {
        log.info("Invoked: getConnectionEndpoint");
        return null;
    }
}
