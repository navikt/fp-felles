package no.nav.vedtak.felles.integrasjon.kodeverk;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.FinnKodeverkListeRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class KodeverkConsumerTest {

    private KodeverkConsumer consumer;

    private KodeverkPortType mockWebservice = mock(KodeverkPortType.class);

    @BeforeEach
    public void setUp() {
        consumer = new KodeverkConsumerImpl(mockWebservice);
    }

    @Test
    public void test_skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_finnKodeverkListe() throws Exception {
        when(mockWebservice.finnKodeverkListe(any(FinnKodeverkListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        var e = assertThrows(IntegrasjonException.class, () -> consumer.finnKodeverkListe(mock(FinnKodeverkListeRequest.class)));
        assertTrue(e.getKode().equals("FP-942048"));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentKodeverk() throws Exception {
        when(mockWebservice.hentKodeverk(any(HentKodeverkRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentKodeverk(mock(HentKodeverkRequest.class)));
        assertTrue(e.getKode().equals("FP-942048"));

    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
