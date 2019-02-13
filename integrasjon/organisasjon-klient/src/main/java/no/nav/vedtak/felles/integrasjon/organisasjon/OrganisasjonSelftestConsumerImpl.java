package no.nav.vedtak.felles.integrasjon.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;

class OrganisasjonSelftestConsumerImpl implements OrganisasjonSelftestConsumer {
    private OrganisasjonV4 port;
    private String endpointUrl;

    public OrganisasjonSelftestConsumerImpl(OrganisasjonV4 port, String endpointUrl) {
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
