package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import no.nav.melding.virksomhet.hendelse.behandling.status.v1.BehandlingAvsluttet;
import no.nav.melding.virksomhet.hendelse.behandling.status.v1.BehandlingOpprettet;
import no.nav.vedtak.felles.integrasjon.jms.QueueSelftest;

public interface SakOgBehandlingClient extends QueueSelftest {

    void sendBehandlingOpprettet(BehandlingOpprettet behandlingOpprettet);

    void sendBehandlingAvsluttet(BehandlingAvsluttet behandlingAvsluttet);
}
