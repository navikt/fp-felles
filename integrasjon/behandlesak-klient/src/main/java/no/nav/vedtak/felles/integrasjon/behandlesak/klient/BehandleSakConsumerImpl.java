package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.behandlesak.v2.BehandleSakV2;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakResponse;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSakEksistererAlleredeException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSUgyldigInputException;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class BehandleSakConsumerImpl implements BehandleSakConsumer {
    public static final String SERVICE_IDENTIFIER = "BehandleSakV2";

    private BehandleSakV2 port;

    public BehandleSakConsumerImpl(BehandleSakV2 port) {
        this.port = port;
    }

    @Override
    public WSOpprettSakResponse opprettSak(WSOpprettSakRequest request) throws WSSikkerhetsbegrensningException, WSSakEksistererAlleredeException, WSUgyldigInputException  {
        try {
            return port.opprettSak(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
