package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;

import org.apache.commons.lang3.StringUtils;
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

    @Test
    void testQ() throws Exception {
        URI uri = URI.create("/fpsak/api/behandling/aksjonspunkt-v2?uuid=b3aa9a5f-fc0e-4342-b916-48fc821764ba");
        URI base = URI.create("http://fpsak");
        var r = split(uri.getQuery());
        System.out.println(r);
        var t = ClientBuilder.newClient().target(base).path(uri.getRawPath()).queryParam(r.key, r.value);
        System.out.println(t.getUri());

    }

    private static Query split(String query) {
        var parts = StringUtils.split(query, '=');
        if (parts.length != 2) {
            throw new IllegalArgumentException("Uventet query " + query);
        }
        return new Query(parts[0], parts[1]);
    }

    private static record Query(String key, String value) {

    }
}
