package no.nav.vedtak.felles.integrasjon.medl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class MedlemConsumerTest {

    private MedlemConsumer consumer;

    private MedlemskapV2 mockWebservice = mock(MedlemskapV2.class);

    @BeforeEach
    public void setUp() {
        consumer = new MedlemConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPeriode() throws Exception {
        when(mockWebservice.hentPeriode(any(HentPeriodeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        assertEquals(assertThrows(IntegrasjonException.class, () -> consumer.hentPeriode(mock(HentPeriodeRequest.class))).getKode(), "FP-942048");
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault_hentPeriodeListe() throws Exception {
        when(mockWebservice.hentPeriodeListe(any(HentPeriodeListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));
        assertTrue(assertThrows(IntegrasjonException.class, () -> consumer.hentPeriodeListe(mock(HentPeriodeListeRequest.class))).getKode()
                .equals("FP-942048"));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}
