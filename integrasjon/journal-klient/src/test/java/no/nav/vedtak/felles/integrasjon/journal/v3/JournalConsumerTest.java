package no.nav.vedtak.felles.integrasjon.journal.v3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

@ExtendWith(MockitoExtension.class)
public class JournalConsumerTest {

    private JournalConsumer consumer;

    @Mock
    private JournalV3 mockWebservice;

    @BeforeEach
    public void setUp() {
        consumer = new JournalConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFaul_hentDokument() throws Exception {
        when(mockWebservice.hentDokument(any(HentDokumentRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentDokument(mock(HentDokumentRequest.class)));
        assertEquals(e.getKode(), "F-942048");
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_kjerneJournalpostListe() throws Exception {
        when(mockWebservice.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentKjerneJournalpostListe(mock(HentKjerneJournalpostListeRequest.class)));
        assertEquals(e.getKode(), "F-942048");

    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentDokumentUrl() throws Exception {
        when(mockWebservice.hentDokumentURL(any(HentDokumentURLRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentDokumentURL(mock(HentDokumentURLRequest.class)));
        assertEquals(e.getKode(), "F-942048");
    }

    private static SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
