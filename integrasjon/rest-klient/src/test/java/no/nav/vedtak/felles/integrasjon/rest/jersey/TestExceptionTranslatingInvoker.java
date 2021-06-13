package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(MockitoExtension.class)
public class TestExceptionTranslatingInvoker {
    @Mock
    Invocation i;

    @Test
    void testInvocationTranslation() {
        when(i.invoke(String.class)).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                throw new ProcessingException("OOPS");
            }
        });
        assertThrows(IntegrasjonException.class, () -> new ExceptionTranslatingInvoker().invoke(i, String.class));
    }

    @Test
    void testInvocationTranslationNonDefault() {
        when(i.invoke(String.class)).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                throw new NullPointerException("OOPS");
            }
        });
        assertThrows(TekniskException.class,
                () -> new ExceptionTranslatingInvoker(TekniskException.class).invoke(i, String.class, NullPointerException.class));
    }

}
