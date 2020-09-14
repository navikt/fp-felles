package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import no.nav.vedtak.log.mdc.MDCOperations;

public class CallIdInInterceptorTest {

    private CallIdInInterceptor interceptor = new CallIdInInterceptor();

    @Test
    public void skal_sette_callId_hvis_finnes_i_soap_melding() throws Exception {
        final SoapMessage message = new SoapMessage(Soap11.getInstance());
        final String callerId = MDCOperations.generateCallId();
        Element element = mock(Element.class);
        when(element.getTextContent()).thenReturn(callerId);
        message.getHeaders().add(new Header(MDCOperations.CALLID_QNAME, element));

        interceptor.handleMessage(message);

        assertThat(MDCOperations.getCallId()).isEqualTo(callerId);
    }
}