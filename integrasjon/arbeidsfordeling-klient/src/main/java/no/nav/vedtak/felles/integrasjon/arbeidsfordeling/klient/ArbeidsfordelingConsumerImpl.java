package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class ArbeidsfordelingConsumerImpl implements ArbeidsfordelingConsumer {
    public static final String SERVICE_IDENTIFIER = "ArbeidsfordelingV1";

    private ArbeidsfordelingV1 port;

    public ArbeidsfordelingConsumerImpl(ArbeidsfordelingV1 port) {
        this.port = port;
    }

    @Override
    public FinnBehandlendeEnhetListeResponse finnBehandlendeEnhetListe(FinnBehandlendeEnhetListeRequest request)
            throws FinnBehandlendeEnhetListeUgyldigInput {
        try {
            return port.finnBehandlendeEnhetListe(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public FinnAlleBehandlendeEnheterListeResponse finnAlleBehandlendeEnheterListe(FinnAlleBehandlendeEnheterListeRequest request)
            throws FinnAlleBehandlendeEnheterListeUgyldigInput {
        return port.finnAlleBehandlendeEnheterListe(request);
    }
}
