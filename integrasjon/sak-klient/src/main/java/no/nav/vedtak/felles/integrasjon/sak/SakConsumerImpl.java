package no.nav.vedtak.felles.integrasjon.sak;

import javax.xml.ws.WebServiceException;

import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.binding.SakV1;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class SakConsumerImpl implements SakConsumer {
    public static final String SERVICE_IDENTIFIER = "SakV1";

    private SakV1 port;

    public SakConsumerImpl(SakV1 port) {
        this.port = port;
    }

    @Override
    public FinnSakResponse finnSak(FinnSakRequest request) throws FinnSakForMangeForekomster, FinnSakUgyldigInput {
        try {
            return port.finnSak(request);
        } catch (WebServiceException e) { //NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
