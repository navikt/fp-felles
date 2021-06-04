package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Invocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.ExceptionTranslatingInvoker;

@ExtendWith(MockitoExtension.class)
class TestTokenX {

    @Mock
    Invocation i;

    @Test
    void testAudience() {
        System.setProperty("pdl-api.default", "dev-fss");
        var uri = URI.create("http://pdl-api.default/graphql");
        var aud = new TokenXAudienceGenerator().audience(uri);
        assertEquals("dev-fss:default:pdl-api", aud.asAudience());
    }

    @Test
    void testInvocationTranslation() {
        when(i.invoke(String.class)).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                throw new SocketTimeoutException();
            }
        });
        assertThrows(IntegrasjonException.class, () -> new ExceptionTranslatingInvoker(List.of(IOException.class)).invoke(i, String.class));
    }

}
