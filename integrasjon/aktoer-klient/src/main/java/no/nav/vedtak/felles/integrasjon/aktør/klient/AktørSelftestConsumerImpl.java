package no.nav.vedtak.felles.integrasjon.aktør.klient;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;

/**
 *
 * @deprecated Bruk PDL
 *
 */
@Deprecated(since = "3.0.x", forRemoval = true)
class AktørSelftestConsumerImpl implements AktørSelftestConsumer {
    private AktoerV2 port;
    private String endpointUrl;

    public AktørSelftestConsumerImpl(AktoerV2 port, String endpointUrl) {
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
