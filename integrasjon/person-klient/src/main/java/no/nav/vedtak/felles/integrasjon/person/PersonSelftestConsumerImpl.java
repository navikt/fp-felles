package no.nav.vedtak.felles.integrasjon.person;

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;

/**
 *
 * @deprecated, Bruk PDL
 *
 */
@Deprecated
class PersonSelftestConsumerImpl implements PersonSelftestConsumer {
    private PersonV3 port;
    private String endpointUrl;

    public PersonSelftestConsumerImpl(PersonV3 port, String endpointUrl) {
        this.port = port;
        this.endpointUrl = endpointUrl;
    }

    @Override
    public void ping() {
        port.ping();
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl;
    }
}
