package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import java.time.LocalDate;
import java.util.Optional;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSFerdigstillOppgaveException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSAktor;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSAktorType;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveResponse;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOppgave;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OppretteOppgaveFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class BehandleoppgaveConsumerImpl implements BehandleoppgaveConsumer {
    public static final String SERVICE_IDENTIFIER = "BehandleOppgaveV1";

    private BehandleOppgaveV1 port;

    public BehandleoppgaveConsumerImpl(BehandleOppgaveV1 port) {
        this.port = port;
    }

    @Override
    public WSOpprettOppgaveResponse opprettOppgave(OpprettOppgaveRequest request) {
        WSOpprettOppgaveResponse opprettOppgaveResponse;
		try {
			opprettOppgaveResponse = port.opprettOppgave(convertToWSRequest(request));
		} catch (WSSikkerhetsbegrensningException e) {
			throw FeilFactory.create(OppretteOppgaveFeil.class).opprettOppgaveSikkerhetsbegrensing(e).toException();
		}  catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
        if (opprettOppgaveResponse.getOppgaveId() == null) {
            throw FeilFactory.create(OppretteOppgaveFeil.class).fikkUgyldigResponse().toException();
        }
        return opprettOppgaveResponse;
    }

    @Override
    public WSFerdigstillOppgaveResponse ferdigstillOppgave(FerdigstillOppgaveRequestMal request) {
        WSFerdigstillOppgaveResponse ferdigstillOppgaveResponse = new WSFerdigstillOppgaveResponse();
		try {
			ferdigstillOppgaveResponse = port.ferdigstillOppgave(convertToWSRequest(request));
		} catch (WSFerdigstillOppgaveException e) { // NOSONAR
			throw FeilFactory.create(FerdigstillOppgaveFeil.class).fikkFeilIResponse(e.getMessage()).toException(); 
		} catch (WSSikkerhetsbegrensningException e) {
			throw FeilFactory.create(FerdigstillOppgaveFeil.class).ferdigstillOppgaveSikkerhetsbegrensing(e).toException();
		} catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
        return ferdigstillOppgaveResponse;
    }

    WSOpprettOppgaveRequest convertToWSRequest(OpprettOppgaveRequest request) {
    	WSOppgave oppgave = new WSOppgave();
        oppgave.setSaksnummer(request.getSaksnummer());
        oppgave.setAnsvarligEnhetId(request.getAnsvarligEnhetId());
        oppgave.setFagomradeKode(request.getFagomradeKode().toString());        
        oppgave.setGjelderBruker(byggWSAktor(request.getFnr(), request.getBrukerTypeKode()));

        oppgave.setAktivFra(DateUtil.convertToXMLGregorianCalendar(request.getAktivFra().atStartOfDay()));

        Optional<LocalDate> aktivTil = request.getAktivTil();
        if (aktivTil.isPresent()) {
            oppgave.setAktivTil(DateUtil.convertToXMLGregorianCalendar(aktivTil.get().atStartOfDay()));
        }

        oppgave.setOppgavetypeKode(request.getOppgavetypeKode());
        oppgave.setUnderkategoriKode(request.getUnderkategoriKode());
        oppgave.setPrioritetKode(request.getPrioritetKode().toString());
        oppgave.setBeskrivelse(request.getBeskrivelse());
        oppgave.setLest(request.isLest());
        oppgave.setDokumentId(request.getDokumentId());

        oppgave.setNormDato(DateUtil.convertToXMLGregorianCalendar(request.getNormertBehandlingsTidInnen()));
        oppgave.setMottattDato(DateUtil.convertToXMLGregorianCalendar(request.getMottattDato()));
        WSOpprettOppgaveRequest result = new WSOpprettOppgaveRequest();
        result.setOpprettetAvEnhetId(request.getOpprettetAvEnhetId());
        result.setWsOppgave(oppgave);
        return result;
    }

    private WSFerdigstillOppgaveRequest convertToWSRequest(FerdigstillOppgaveRequestMal request) {
    	WSFerdigstillOppgaveRequest result = new WSFerdigstillOppgaveRequest();
        result.setFerdigstiltAvEnhetId(request.getFerdigstiltAvEnhetId());
        result.setOppgaveId(request.getOppgaveId());
        return result;
    }

    private WSAktor byggWSAktor(String ident, BrukerType brukerType) {
    	WSAktor wsAktor = new WSAktor();
    	wsAktor.setIdent(ident);
    	if (brukerType == null || BrukerType.BLANK == brukerType) {
    		return wsAktor;
    	} else if (BrukerType.ORGANISASJON == brukerType) {
    		wsAktor.setAktorType(WSAktorType.ORGANISASJON);
    	} else if (BrukerType.PERSON == brukerType) {
    		wsAktor.setAktorType(WSAktorType.PERSON);
    	} else {
    		wsAktor.setAktorType(WSAktorType.UKJENT);
    	}
    	return wsAktor;
    }
}
