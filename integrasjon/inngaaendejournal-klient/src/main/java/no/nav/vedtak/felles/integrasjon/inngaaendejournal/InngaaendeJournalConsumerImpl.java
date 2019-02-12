package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class InngaaendeJournalConsumerImpl implements InngaaendeJournalConsumer {
    public static final String SERVICE_IDENTIFIER = "InngaaendeJournalV1";

    private InngaaendeJournalV1 port;

    public InngaaendeJournalConsumerImpl(InngaaendeJournalV1 port) {
        this.port = port;
    }

    @Override
    public HentJournalpostResponse hentJournalpost(HentJournalpostRequest request) throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende, HentJournalpostSikkerhetsbegrensning, HentJournalpostUgyldigInput {
        try {
            return port.hentJournalpost(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public UtledJournalfoeringsbehovResponse utledJournalfoeringsbehov(UtledJournalfoeringsbehovRequest request) throws UtledJournalfoeringsbehovSikkerhetsbegrensning, UtledJournalfoeringsbehovUgyldigInput, UtledJournalfoeringsbehovJournalpostKanIkkeBehandles, UtledJournalfoeringsbehovJournalpostIkkeFunnet, UtledJournalfoeringsbehovJournalpostIkkeInngaaende {
        try {
            return port.utledJournalfoeringsbehov(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
