package no.nav.vedtak.felles.integrasjon.person;

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

import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class PersonConsumerTest {

    private PersonConsumer consumer;

    private PersonV3 mockWebservice = mock(PersonV3.class);

    @BeforeEach
    public void setUp() {
        consumer = new PersonConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPerson() throws Exception {
        when(mockWebservice.hentPerson(any(HentPersonRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        assertTrue(assertThrows(IntegrasjonException.class, () -> consumer.hentPersonResponse(mock(HentPersonRequest.class))).getKode()
                .equals("FP-942048"));
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPersonHistorikk() throws Exception {
        when(mockWebservice.hentPersonhistorikk(any(HentPersonhistorikkRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        assertTrue(assertThrows(IntegrasjonException.class, () -> consumer.hentPersonhistorikkResponse(mock(HentPersonhistorikkRequest.class)))
                .getKode().equals("FP-942048"));

    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentGeografiskTilknytning() throws Exception {
        when(mockWebservice.hentGeografiskTilknytning(any(HentGeografiskTilknytningRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        assertTrue(assertThrows(IntegrasjonException.class, () -> consumer.hentGeografiskTilknytning(mock(HentGeografiskTilknytningRequest.class)))
                .getKode().equals("FP-942048"));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
