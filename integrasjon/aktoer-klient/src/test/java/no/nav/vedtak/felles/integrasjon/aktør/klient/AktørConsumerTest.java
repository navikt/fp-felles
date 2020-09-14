package no.nav.vedtak.felles.integrasjon.aktør.klient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.vedtak.exception.IntegrasjonException;

public class AktørConsumerTest {

    AktoerV2 mockAktoerV2 = mock(AktoerV2.class);
    AktørConsumer consumer;

    @BeforeEach
    public void setUp() {
        consumer = new AktørConsumerImpl(mockAktoerV2);
    }

    @Test
    public void skalReturnereTomOptionalNårServiceKasterHentAktoerIdForIdenPersonIkkeFunnet() throws Exception {
        HentAktoerIdForIdentPersonIkkeFunnet fault = new HentAktoerIdForIdentPersonIkkeFunnet("status: S511002F", null);
        when(mockAktoerV2.hentAktoerIdForIdent(any())).thenThrow(fault);

        Optional<String> res = consumer.hentAktørIdForPersonIdent("123");
        assertThat(res).isEmpty();
    }

    @Test
    public void skalKasteIntegrasjonExceptionNårServiceKasterFeilMedStatusS511002F() throws Exception {
        when(mockAktoerV2.hentAktoerIdForIdent(any())).thenThrow(opprettSOAPFaultException("status: S511002F"));

        // expectedException.expect(IntegrasjonException.class);
        // expectedException.expectMessage("F-502945");

        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentAktørIdForPersonIdent("123"));
        System.out.println(e.getMessage());
        assertTrue(e.getMessage().contains("F-502945"));

    }

    @Test
    public void skalKasteIntegrasjonExceptionMedFeilmeldingForAndreFeil() throws Exception {
        when(mockAktoerV2.hentAktoerIdForIdent(any())).thenThrow(opprettSOAPFaultException("annen feil"));

        var e = assertThrows(IntegrasjonException.class, () -> consumer.hentAktørIdForPersonIdent("123"));
        assertTrue(e.getMessage().contains("FP-942048"));

        // expectedException.expect(IntegrasjonException.class);
        // expectedException.expectMessage("FP-942048");

    }

    private static SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}