package no.nav.vedtak.sikkerhet.jaspic.soap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ServiceLoader;

import jakarta.servlet.annotation.WebServlet;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.jaspic.DelegatedProtectedResource;

class SoapProtectedResourceTest {

    @Test
    void load_service() {
        ClassLoader classLoader = SoapProtectedResourceTest.class.getClassLoader();
        assertThat(ServiceLoader.load(DelegatedProtectedResource.class, classLoader)).anyMatch(p -> SoapProtectedResource.class.equals(p.getClass()));
    }

    @WebServlet(urlPatterns = {"/tjenester", "/tjenester/", "/tjenester/*"}, loadOnStartup = 1)
    private class WSS4JProtectedServletTestImpl implements WSS4JProtectedServlet {

        @Override
        public boolean isProtectedWithAction(String pathInfo, String requiredAction) {
            return true;
        }
    }
}
