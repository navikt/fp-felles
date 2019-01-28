package no.nav.vedtak.felles.integrasjon.kodeverk;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.FinnKodeverkListeRequest;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.HentKodeverkRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class KodeverkConsumerTest {

    private KodeverkConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private KodeverkPortType mockWebservice = mock(KodeverkPortType.class);

    @Before
    public void setUp() {
        consumer = new KodeverkConsumerImpl(mockWebservice);
    }

    @Test
    public void test_skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_finnKodeverkListe() throws Exception {
        when(mockWebservice.finnKodeverkListe(any(FinnKodeverkListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnKodeverkListe(mock(FinnKodeverkListeRequest.class));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentKodeverk() throws Exception {
        when(mockWebservice.hentKodeverk(any(HentKodeverkRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentKodeverk(mock(HentKodeverkRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
