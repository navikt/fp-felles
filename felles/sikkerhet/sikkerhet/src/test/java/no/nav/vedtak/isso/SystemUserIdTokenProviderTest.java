package no.nav.vedtak.isso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.oidc.VlIOException;

public class SystemUserIdTokenProviderTest {

    @Test
    public void skal_hente_token() throws Exception {
        OpenAMHelper openAMHelper = mock(OpenAMHelper.class);
        Random random = mock(Random.class);
        OidcCredential oidcToken = new OidcCredential("dummy.oidc.token");
        when(openAMHelper.getToken()).thenReturn(new IdTokenAndRefreshToken(oidcToken, "dummy.refresh.token"));

        assertThat(SystemUserIdTokenProvider.fetchIdToken(0, openAMHelper, random)).isEqualTo(oidcToken);

        verify(openAMHelper, times(1)).getToken();
    }

    @Test
    public void skal_gjøre_retry_når_henting_av_token_feiler_og_til_slutt_feile() throws Exception {
        OpenAMHelper openAMHelper = mock(OpenAMHelper.class);
        Random random = mock(Random.class);
        Mockito.when(random.nextInt(anyInt())).thenReturn(-1); //HAXX setter -1 for at testen skal slippe å sove
        when(openAMHelper.getToken()).thenThrow(mock(VlIOException.class));
        try {
            SystemUserIdTokenProvider.fetchIdToken(0, openAMHelper, random);
            throw new AssertionError("Forventet exception");
        } catch (IntegrasjonException e) {
            assertThat(e.getFeil().getLogLevel()).isEqualTo(Level.ERROR);
        }

        verify(openAMHelper, times(10)).getToken();
    }

    @Test
    public void sovetid_skal_være_minimum_forventet_tid_for_å_hente_token() throws Exception {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(0);
        assertThat(SystemUserIdTokenProvider.sovetid(random)).isEqualTo(SystemUserIdTokenProvider.ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS);

    }

    @Test
    public void sovetid_skal_et_tilfeldig_heltall_ganget_med_forventet_tid_For_å_hente_token() throws Exception {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(1);
        assertThat(SystemUserIdTokenProvider.sovetid(random)).isEqualTo(SystemUserIdTokenProvider.ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS * 2);
        when(random.nextInt(anyInt())).thenReturn(2);
        assertThat(SystemUserIdTokenProvider.sovetid(random)).isEqualTo(SystemUserIdTokenProvider.ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS * 3);
        when(random.nextInt(anyInt())).thenReturn(3);
        assertThat(SystemUserIdTokenProvider.sovetid(random)).isEqualTo(SystemUserIdTokenProvider.ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS * 4);
        when(random.nextInt(anyInt())).thenReturn(4);
        assertThat(SystemUserIdTokenProvider.sovetid(random)).isEqualTo(SystemUserIdTokenProvider.ESTIMERT_TID_FOR_Å_HENTE_TOKEN_MILLIS * 5);
    }

}