package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.InfotrygdSakV1;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class InfotrygdSakConsumerImpl implements InfotrygdSakConsumer {
    public static final String SERVICE_IDENTIFIER = "InfotrygdSakV1";

    private InfotrygdSakV1 port;

    public InfotrygdSakConsumerImpl(InfotrygdSakV1 port) {
        this.port = port;
    }

    @Override
    public FinnSakListeResponse finnSakListe(FinnSakListeRequest finnSakListeRequest) throws FinnSakListePersonIkkeFunnet, FinnSakListeSikkerhetsbegrensning, FinnSakListeUgyldigInput {
        try {
            return port.finnSakListe(finnSakListeRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}