package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.log.mdc.MDCOperations;

public class CallIdOutInterceptorTest {

    private CallIdOutInterceptor interceptor; // objektet vi tester

    private SoapMessage mockMessage;
    private List<Header> headers;

    @BeforeEach
    public void setup() {
        interceptor = new CallIdOutInterceptor();
        mockMessage = mock(SoapMessage.class);
        headers = new ArrayList<>();
        when(mockMessage.getHeaders()).thenReturn(headers);
        MDCOperations.remove(MDCOperations.MDC_CALL_ID);
    }

    @Test
    public void test_handleMessage_ok() {
        MDCOperations.putCallId("id123");
        interceptor.handleMessage(mockMessage);
        assertThat(headers.size()).isEqualTo(1);
    }

    @Test
    public void test_handleMessage_noCallId() {
        assertThrows(IllegalStateException.class, () -> interceptor.handleMessage(mockMessage));
    }

    @Test
    public void test_handleMessage_badMessage() {
        assertThrows(IllegalStateException.class, () -> interceptor.handleMessage(mock(Message.class)));
    }
}
