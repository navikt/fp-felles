package no.nav.vedtak.felles.integrasjon.felles.ws;

import no.nav.vedtak.log.mdc.MDCOperations;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.message.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class CallIdOutInterceptorTest {

    private CallIdOutInterceptor interceptor; // objektet vi tester

    @Mock
    private SoapMessage mockMessage;
    private List<Header> headers;

    @BeforeEach
    void setup() {
        interceptor = new CallIdOutInterceptor();
        headers = new ArrayList<>();
        when(mockMessage.getHeaders()).thenReturn(headers);
        MDCOperations.remove(MDCOperations.MDC_CALL_ID);
    }

    @Test
    void test_handleMessage_ok() {
        MDCOperations.putCallId("id123");
        interceptor.handleMessage(mockMessage);
        assertThat(headers.size()).isEqualTo(1);
    }

    //@Test ustabil - virker annenhver gang
    void test_handleMessage_noCallId() {
        assertThrows(IllegalStateException.class, () -> interceptor.handleMessage(mockMessage));
    }

    @Test
    void test_handleMessage_badMessage() {
        assertThrows(IllegalStateException.class, () -> interceptor.handleMessage(mock(Message.class)));
    }
}
