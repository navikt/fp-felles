package no.nav.vedtak.sikkerhet.jaspic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.sikkerhet.ContextPathHolder;

@ExtendWith(MockitoExtension.class)
class TokenLocatorTest {

    @Mock
    private HttpServletRequest requestContext;
    private TokenLocator tokenLocator = new TokenLocator();


    @Test
    void skal_finne_token_i_authorization_header() {
        ContextPathHolder.instance();
        when(requestContext.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().token()).isEqualTo("eyJhbGciOiJS...");
    }

    @Test
    void skal_ikke_finne_token_når_token_ikke_finnes_i_authorization_header() {
        ContextPathHolder.instance();
        when(requestContext.getHeader("Authorization")).thenReturn("");
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();

        when(requestContext.getHeader("Authorization")).thenReturn(null);
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }

    @Test
    void skal_finne_id_token_i_cookie() {
        ContextPathHolder.instance("/k9", "/k9");
        when(requestContext.getCookies()).thenReturn(new Cookie[]{new Cookie(TokenLocator.ID_TOKEN_COOKIE_NAME, "eyJhbGciOiJS...")});
        assertThat(tokenLocator.getToken(requestContext).get().token()).isEqualTo("eyJhbGciOiJS...");
    }

    @Test
    void skal_ikke_finne_id_token_i_cookie_som_har_feil_navn() {
        ContextPathHolder.instance("/k9", "/k9");
        when(requestContext.getCookies()).thenReturn(new Cookie[]{new Cookie("tull", "eyJhbGciOiJS...")});
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }
}
