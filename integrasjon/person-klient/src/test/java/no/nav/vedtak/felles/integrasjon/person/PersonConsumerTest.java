package no.nav.vedtak.felles.integrasjon.person;

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

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class PersonConsumerTest {

    private PersonConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PersonV3 mockWebservice = mock(PersonV3.class);

    @Before
    public void setUp() {
        consumer = new PersonConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPerson() throws Exception {
        when(mockWebservice.hentPerson(any(HentPersonRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentPersonResponse(mock(HentPersonRequest.class));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPersonHistorikk() throws Exception {
        when(mockWebservice.hentPersonhistorikk(any(HentPersonhistorikkRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentPersonhistorikkResponse(mock(HentPersonhistorikkRequest.class));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentGeografiskTilknytning() throws Exception {
        when(mockWebservice.hentGeografiskTilknytning(any(HentGeografiskTilknytningRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentGeografiskTilknytning(mock(HentGeografiskTilknytningRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
