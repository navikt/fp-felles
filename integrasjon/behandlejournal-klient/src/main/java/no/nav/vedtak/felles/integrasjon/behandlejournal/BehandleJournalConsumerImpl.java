package no.nav.vedtak.felles.integrasjon.behandlejournal;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class BehandleJournalConsumerImpl implements BehandleJournalConsumer {
    public static final String SERVICE_IDENTIFIER = "BehandleJournalV3";

    private BehandleJournalV3 port;

    public BehandleJournalConsumerImpl(BehandleJournalV3 port){
        this.port = port;
    }

    @Override
    public JournalfoerNotatResponse journalfoerNotat(JournalfoerNotatRequest request) throws JournalfoerNotatSikkerhetsbegrensning {
        try {
            return port.journalfoerNotat(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
