package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingAvsluttet;
import no.nav.melding.virksomhet.behandlingsstatus.hendelsehandterer.v1.hendelseshandtererbehandlingsstatus.BehandlingOpprettet;
import no.nav.vedtak.felles.integrasjon.jms.QueueSelftest;

public interface SakOgBehandlingClient extends QueueSelftest {

    void sendBehandlingOpprettet(BehandlingOpprettet behandlingOpprettet);

    void sendBehandlingAvsluttet(BehandlingAvsluttet behandlingAvsluttet);
}
