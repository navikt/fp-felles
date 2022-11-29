package no.nav.vedtak.felles.integrasjon.felles.ws;

import no.nav.vedtak.log.mdc.MDCOperations;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Element;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallIdInInterceptorTest {
    @Mock
    private Element element;
    private CallIdInInterceptor interceptor = new CallIdInInterceptor();

    @Test
    void skal_sette_callId_hvis_finnes_i_soap_melding() throws Exception {
        var message = new SoapMessage(Soap11.getInstance());
        String callerId = MDCOperations.generateCallId();
        when(element.getTextContent()).thenReturn(callerId);
        message.getHeaders().add(new Header(MDCOperations.CALLID_QNAME, element));
        interceptor.handleMessage(message);
        assertThat(MDCOperations.getCallId()).isEqualTo(callerId);
    }
}
