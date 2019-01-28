package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

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

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class ArbeidsfordelingConsumerTest {

    ArbeidsfordelingConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    ArbeidsfordelingV1 mockWebservice = mock(ArbeidsfordelingV1.class);

    @Before
    public void setUp() throws Exception {
        consumer = new ArbeidsfordelingConsumerImpl(mockWebservice);
    }

    @Test
    public void skalFangeSoapFaulOgKasteFeilmelding() throws Exception {
        when(mockWebservice.finnBehandlendeEnhetListe(any(FinnBehandlendeEnhetListeRequest.class))).thenThrow(opprettSOAPFaultException());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnBehandlendeEnhetListe(mock(FinnBehandlendeEnhetListeRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException() throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString("testing");
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}