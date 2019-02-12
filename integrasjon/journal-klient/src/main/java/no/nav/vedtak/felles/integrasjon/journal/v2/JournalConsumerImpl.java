package no.nav.vedtak.felles.integrasjon.journal.v2;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentURLSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v2.binding.JournalV2;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentURLResponse;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentJournalpostListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class JournalConsumerImpl implements JournalConsumer {
    public static final String SERVICE_IDENTIFIER = "JournalV2";

    private JournalV2 port;

    public JournalConsumerImpl(JournalV2 port) {
        this.port = port;
    }

    @Override
    public HentDokumentResponse hentDokument(HentDokumentRequest request) throws HentDokumentDokumentIkkeFunnet, HentDokumentSikkerhetsbegrensning {
        try {
            return port.hentDokument(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public HentJournalpostListeResponse hentJournalpostListe(HentJournalpostListeRequest request) throws HentJournalpostListeSikkerhetsbegrensning {
        try {
            return port.hentJournalpostListe(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public HentDokumentURLResponse hentDokumentURL(HentDokumentURLRequest request) throws HentDokumentURLDokumentIkkeFunnet, HentDokumentURLSikkerhetsbegrensning {
        try {
            return port.hentDokumentURL(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
