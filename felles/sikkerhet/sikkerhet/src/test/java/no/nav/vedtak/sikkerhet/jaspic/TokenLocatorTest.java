package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TokenLocatorTest {

    private HttpServletRequest requestContext = Mockito.mock(HttpServletRequest.class);
    private TokenLocator tokenLocator = new TokenLocator();

    @Test
    public void skal_finne_token_i_authorization_header() {
        Mockito.when(requestContext.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().getToken()).isEqualTo("eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().isFromCookie()).isFalse();
    }

    @Test
    public void skal_ikke_finne_token_når_token_ikke_finnes_i_authorization_header() {
        Mockito.when(requestContext.getHeader("Authorization")).thenReturn("");
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();

        Mockito.when(requestContext.getHeader("Authorization")).thenReturn(null);
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }

    @Test
    public void skal_finne_id_token_i_cookie() {
        Cookie cookie = new Cookie(ID_TOKEN_COOKIE_NAME, "eyJhbGciOiJS...");
        Cookie[] cookies = { cookie };
        Mockito.when(requestContext.getCookies()).thenReturn(cookies);
        assertThat(tokenLocator.getToken(requestContext).get().getToken()).isEqualTo("eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().isFromCookie()).isTrue();
    }

    @Test
    public void skal_ikke_finne_id_token_i_cookie_som_har_feil_navn() {
        Cookie cookie = new Cookie("tull", "eyJhbGciOiJS...");
        Cookie[] cookies = { cookie };
        Mockito.when(requestContext.getCookies()).thenReturn(cookies);
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }

    @Test
    public void skal_finne_refresh_token_i_cookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "123fas1-1234-a1r2");
        Cookie[] cookies = { cookie };
        Mockito.when(requestContext.getCookies()).thenReturn(cookies);
        assertThat(tokenLocator.getRefreshToken(requestContext).get()).isEqualTo("123fas1-1234-a1r2");
    }

    @Test
    public void skal_ikke_finne_refreshtoken_i_cookie_som_har_feil_navn() {
        Cookie cookie = new Cookie("tull", "123fas1-1234-a1r2");
        Cookie[] cookies = { cookie };
        Mockito.when(requestContext.getCookies()).thenReturn(cookies);
        assertThat(tokenLocator.getRefreshToken(requestContext)).isNotPresent();
    }
}