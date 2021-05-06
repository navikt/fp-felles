package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenLocatorTest {

    @Mock
    private HttpServletRequest requestContext;
    private TokenLocator tokenLocator = new TokenLocator();

    @Test
    void skal_finne_token_i_authorization_header() {
        when(requestContext.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().getToken()).isEqualTo("eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().isFromCookie()).isFalse();
    }

    @Test
    void skal_ikke_finne_token_når_token_ikke_finnes_i_authorization_header() {
        when(requestContext.getHeader("Authorization")).thenReturn("");
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();

        when(requestContext.getHeader("Authorization")).thenReturn(null);
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }

    @Test
    void skal_finne_id_token_i_cookie() {
        when(requestContext.getCookies()).thenReturn(new Cookie[] { new Cookie(ID_TOKEN_COOKIE_NAME, "eyJhbGciOiJS...") });
        assertThat(tokenLocator.getToken(requestContext).get().getToken()).isEqualTo("eyJhbGciOiJS...");
        assertThat(tokenLocator.getToken(requestContext).get().isFromCookie()).isTrue();
    }

    @Test
    void skal_ikke_finne_id_token_i_cookie_som_har_feil_navn() {
        when(requestContext.getCookies()).thenReturn(new Cookie[] { new Cookie("tull", "eyJhbGciOiJS...") });
        assertThat(tokenLocator.getToken(requestContext)).isNotPresent();
    }

    @Test
    void skal_finne_refresh_token_i_cookie() {
        when(requestContext.getCookies()).thenReturn(new Cookie[] { new Cookie(REFRESH_TOKEN_COOKIE_NAME, "123fas1-1234-a1r2") });
        assertThat(tokenLocator.getRefreshToken(requestContext).get()).isEqualTo("123fas1-1234-a1r2");
    }

    @Test
    void skal_ikke_finne_refreshtoken_i_cookie_som_har_feil_navn() {
        when(requestContext.getCookies()).thenReturn(new Cookie[] { new Cookie("tull", "123fas1-1234-a1r2") });
        assertThat(tokenLocator.getRefreshToken(requestContext)).isNotPresent();
    }
}