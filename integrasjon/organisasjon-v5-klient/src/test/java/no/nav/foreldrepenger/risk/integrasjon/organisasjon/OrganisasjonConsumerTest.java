package no.nav.foreldrepenger.risk.integrasjon.organisasjon;

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

import no.nav.foreldrepenger.risk.integrasjon.organisasjon.hent.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5;
import no.nav.vedtak.exception.IntegrasjonException;

public class OrganisasjonConsumerTest {

    private OrganisasjonConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OrganisasjonV5 mockWebservice = mock(OrganisasjonV5.class);

    @Before
    public void setUp() {
        consumer = new OrganisasjonConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.hentOrganisasjon(any(no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        HentOrganisasjonRequest req = mock(HentOrganisasjonRequest.class);
        when(req.getOrgnummer()).thenReturn("1234");

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentOrganisasjon(req);
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}