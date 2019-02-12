package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;

public class ArbeidsforholdSelftestConsumerImpl implements ArbeidsforholdSelftestConsumer {
    private ArbeidsforholdV3 port;
    private String endpointUrl;

    public ArbeidsforholdSelftestConsumerImpl(ArbeidsforholdV3 port, String endpointUrl) {
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
