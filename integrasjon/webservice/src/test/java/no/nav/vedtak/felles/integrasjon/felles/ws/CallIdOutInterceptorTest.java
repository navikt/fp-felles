package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;

import no.nav.vedtak.log.mdc.MDCOperations;

public class CallIdOutInterceptorTest {

    private CallIdOutInterceptor interceptor; // objektet vi tester

    private SoapMessage mockMessage;
    private List<Header> headers;

    @Before
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
        try {
            interceptor.handleMessage(mockMessage);
            fail("forventet exception");
        } catch (IllegalStateException e) {
            // ok
        }
    }

    @Test
    public void test_handleMessage_badMessage() {
        try {
            interceptor.handleMessage(mock(Message.class));
            fail("forventet exception");
        } catch (IllegalStateException e) {
            // ok
        }
    }
}
