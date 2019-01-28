package no.nav.vedtak.felles.integrasjon.oppgave;


import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.HentOppgaveOppgaveIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeFilter;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeSok;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;
import no.nav.vedtak.util.StringUtils;

public class OppgaveConsumerImpl implements OppgaveConsumer {
    public static final String SERVICE_IDENTIFIER = "OppgaveV3";

    private OppgaveV3 port;

    public OppgaveConsumerImpl(OppgaveV3 port) {
        this.port = port;
    }

    @Override
    public FinnOppgaveListeResponse finnOppgaveListe(FinnOppgaveListeRequestMal request) {
        try {
            return port.finnOppgaveListe(convertToWSRequest(request));
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public HentOppgaveResponse hentOppgave(HentOppgaveRequest request) throws HentOppgaveOppgaveIkkeFunnet {
        try {
            return port.hentOppgave(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private FinnOppgaveListeRequest convertToWSRequest(FinnOppgaveListeRequestMal request) {
        FinnOppgaveListeRequest result = new FinnOppgaveListeRequest();

        result.setSok(mapSok(request.getSok()));

        if (request.getFilter() != null) {
            result.setFilter(mapFilter(request.getFilter()));
        }
        if (request.getSorteringKode() != null) {
            result.setSorteringKode(request.getSorteringKode());
        }
        if (!StringUtils.nullOrEmpty(request.getIkkeTidligereFordeltTil())) {
            result.setIkkeTidligereFordeltTil(request.getIkkeTidligereFordeltTil());
        }

        return result;
    }

    private FinnOppgaveListeSok mapSok(FinnOppgaveListeSokMal sokMal) {
        FinnOppgaveListeSok oppgaveListeSok = new FinnOppgaveListeSok();
        oppgaveListeSok.setAnsvarligEnhetId(sokMal.getAnsvarligEnhetId());
        oppgaveListeSok.setBrukerId(sokMal.getBrukerId());
        oppgaveListeSok.setSakId(sokMal.getSakId());

        return oppgaveListeSok;
    }

    private FinnOppgaveListeFilter mapFilter(FinnOppgaveListeFilterMal filterMal) {
        FinnOppgaveListeFilter oppgaveListeFilter = new FinnOppgaveListeFilter();
        oppgaveListeFilter.setOpprettetEnhetId(filterMal.getOpprettetEnhetId());
        oppgaveListeFilter.setOpprettetEnhetNavn(filterMal.getOpprettetEnhetNavn());
        oppgaveListeFilter.setAnsvarligEnhetNavn(filterMal.getAnsvarligEnhetNavn());

        if (filterMal.getOppgavetypeKodeListe() != null) {
            List<String> oppgavetypeKodeListe = oppgaveListeFilter.getOppgavetypeKodeListe();
            oppgavetypeKodeListe.addAll(filterMal.getOppgavetypeKodeListe());
        }

        if (filterMal.getBrukertypeKodeListe() != null) {
            List<String> brukertypeKodeListe = oppgaveListeFilter.getBrukertypeKodeListe();
            brukertypeKodeListe.addAll(filterMal.getBrukertypeKodeListe());
        }

        return oppgaveListeFilter;
    }
}
