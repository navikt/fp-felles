package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class BehandleInngaaendeJournalConsumerImpl implements BehandleInngaaendeJournalConsumer {
    public static final String SERVICE_IDENTIFIER = "BehandleInngaaendeJournalV1";

    private BehandleInngaaendeJournalV1 port;

    public BehandleInngaaendeJournalConsumerImpl(BehandleInngaaendeJournalV1 port) {
        this.port = port;
    }

    @Override
    public void ferdigstillJournalfoering(FerdigstillJournalfoeringRequest request) throws FerdigstillJournalfoeringFerdigstillingIkkeMulig, FerdigstillJournalfoeringJournalpostIkkeInngaaende, FerdigstillJournalfoeringUgyldigInput, FerdigstillJournalfoeringSikkerhetsbegrensning, FerdigstillJournalfoeringObjektIkkeFunnet {
        try {
            port.ferdigstillJournalfoering(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public void oppdaterJournalpost(OppdaterJournalpostRequest request) throws OppdaterJournalpostSikkerhetsbegrensning, OppdaterJournalpostOppdateringIkkeMulig, OppdaterJournalpostUgyldigInput, OppdaterJournalpostJournalpostIkkeInngaaende, OppdaterJournalpostObjektIkkeFunnet {
        try {
            port.oppdaterJournalpost(request);
        }  catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
