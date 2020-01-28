package no.nav.foreldrepenger.risk.integrasjon.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5;

class OrganisasjonSelftestConsumerImpl implements OrganisasjonSelftestConsumer {
    private OrganisasjonV5 port;
    private String endpointUrl;

    public OrganisasjonSelftestConsumerImpl(OrganisasjonV5 port, String endpointUrl) {
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
