package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtUtilTest {

    @Test
    public void skal_kunne_pelle_ut_payload_fra_jwt() throws Exception {
        String etJWT = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICItcVcwcGVhdFUwWFNKa3AtY3JlT19RIiwgInN1YiI6ICJ1MTM5MTU4IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI3Y2Y5M2ZiOS00NmU1LTQ4MjUtYjU4Ni1jZjU4ZDdiOWIyNDYtMTAwMjUwNjgiLCAiaXNzIjogImh0dHBzOi8vaXNzby10LmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJhdWQiOiAiT0lEQyIsICJjX2hhc2giOiAiUUh1Mm8xWU9aVTFGN2EwTkYydFo5ZyIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjAxZDNhMGRlLTA3NzctNGNkMy1iNmE0LWJkN2RkNzAyMjE1ZiIsICJhenAiOiAiT0lEQyIsICJhdXRoX3RpbWUiOiAxNDk0ODQ3NzgyLCAicmVhbG0iOiAiLyIsICJleHAiOiAxNDk0ODUxMzgyLCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE0OTQ4NDc3ODIgfQ.UH6nMEPT4ogNVDS0UFXp0w1kwAE2zIo7eE8P_Cm6LBX7t8BegqLX_1XEXEcJ5Zqgact4Zkf3_LfN_R3OgcX2z1_6H8iVPMNf7XRv-N1WJxNCyW970lvw30xOSL9sd76xiQJ6Q3Hc0PfoJpkVQzZEzHYzAHJ9SbQ-a9YtPlXMf0OzI91W0gL8O49Susnpy7Fy3UcskNpws8nPrjDTdHeneMtAtz0f6XaHdgLgH9F4LKl9xj43nOweKWaE6u_0FqCgiFpX8CaV39-nvQNJv0cgXZQjaxP7dPryVZwes6SsnzfiTofIuVI26GgnDtqu2a2qqaOrQ3Ai7aIpNS_dNrY3PQ";

        String payload = JwtUtil.getJwtBody(etJWT);
        payload = payload.replaceAll("=", ""); // ignorer '=' på slutten av strengen, da dette bare er padding
        assertThat(payload).isEqualTo(
                "eyAiYXRfaGFzaCI6ICItcVcwcGVhdFUwWFNKa3AtY3JlT19RIiwgInN1YiI6ICJ1MTM5MTU4IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI3Y2Y5M2ZiOS00NmU1LTQ4MjUtYjU4Ni1jZjU4ZDdiOWIyNDYtMTAwMjUwNjgiLCAiaXNzIjogImh0dHBzOi8vaXNzby10LmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJhdWQiOiAiT0lEQyIsICJjX2hhc2giOiAiUUh1Mm8xWU9aVTFGN2EwTkYydFo5ZyIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjAxZDNhMGRlLTA3NzctNGNkMy1iNmE0LWJkN2RkNzAyMjE1ZiIsICJhenAiOiAiT0lEQyIsICJhdXRoX3RpbWUiOiAxNDk0ODQ3NzgyLCAicmVhbG0iOiAiLyIsICJleHAiOiAxNDk0ODUxMzgyLCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE0OTQ4NDc3ODIgfQ");
    }

    @Test
    public void skal_feile_dersom_input_er_helt_feil() throws Exception {

        var e = assertThrows(VLException.class, () -> JwtUtil.getJwtBody("tull.og.tøys"));
        assertTrue(e.getMessage().contains("Feil ved parsing av JWT"));
    }

    @Test
    public void skal_hente_clientName_fra_azp_claim() throws Exception {
        /*
         * JWT-body { "iss": "https://foo.bar.adeo.no/openam/oauth2", "exp": 1527256661,
         * "jti": "IfOmOW1mohKVRutF1n-QhA", "iat": 1527253061, "sub": "demo", "aud":
         * "foo", "azp": "bar" }
         */
        String jwt = "eyJraWQiOiIxIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2Zvby5iYXIuYWRlby5uby9vcGVuYW0vb2F1dGgyIiwiZXhwIjoxNTI3MjU2NjYxLCJqdGkiOiJJZk9tT1cxbW9oS1ZSdXRGMW4tUWhBIiwiaWF0IjoxNTI3MjUzMDYxLCJzdWIiOiJkZW1vIiwiYXVkIjoiZm9vIiwiYXpwIjoiYmFyIn0.H43gzgXzUvOQoJcsW6V5QuOSzygLWddbZXPUgzFW2bg09W6j3aVncWglNyRb6RGXIJD8YmJJePWP0xZ5FP9u7qEwvowBE_hQFOfbaM4u674sd6hvupZtk1eDLacU38owANwIoQczBcaTb5KZdwyfWGlsXAG_M3G95a6WxyCJo4WBW3HCwESOYvQep76EzTRIjaFgEKWjfaISdLXtUVWF3UJZaGnCxpiebVQaU81IOI3HYvrX3cVrTlM7QSg5wrFwI2yUl-h49qo_ibBbB-rT976gBeEu26RX2OSZxHHUzjcsqUCLfafje2EPv1Qy5wOlDpGcSoLa7AjtauBtin1Nsg";
        var claims = JwtUtil.getClaims(jwt);
        String clientName = JwtUtil.getClientName(claims);

        assertThat(clientName).isEqualTo("bar");
    }

    @Test
    public void skal_hente_clientName_fra_aud_hvis_azp_claim_mangler() throws Exception {
        /*
         * JWT-body { "iss": "https://foo.bar.adeo.no/openam/oauth2", "exp": 1527256661,
         * "jti": "yzuc_Rdd_6Imo2-4ErfCTw", "iat": 1527253061, "sub": "demo", "aud":
         * "foo" }
         */
        String jwt = "eyJraWQiOiIxIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2Zvby5iYXIuYWRlby5uby9vcGVuYW0vb2F1dGgyIiwiZXhwIjoxNTI3MjU2NjYxLCJqdGkiOiJ5enVjX1JkZF82SW1vMi00RXJmQ1R3IiwiaWF0IjoxNTI3MjUzMDYxLCJzdWIiOiJkZW1vIiwiYXVkIjoiZm9vIn0.HbrqyYdrL1JS6c3n_Tr70QCa0bVdHHD0t_vRbFs2_e6fU78dGNGi2KQPdEnmw419aM8Oh-pmdPXito_roG9KX2Nq7PcFJxXhBXhlubfGisFfqzdfwZDZFW4iJl0amLc4nPJ3Yw3O447lGVM_hpUEDRuTJrD18b_Tajp-peinydupXpiFG1vBrR47gmCLg6jhWuBWTsvUV5F20RFUmTlesy47Xw-80xknySJ7QFhEZDOs4SEXpdp5Rdo4VzGdn2ynQysLH0hhxU-yEjYCj9aKliGZteQYW_ZeogkK4rI09XrFLI5WBdOr9XWyavo6QK7Ba9MCmPNBqIguuUbWpc39xA";
        var claims = JwtUtil.getClaims(jwt);
        String clientName = JwtUtil.getClientName(claims);

        assertThat(clientName).isEqualTo("foo");
    }

    @Test
    public void kan_ikke_hente_clientName_hvis_azp_claim_mangler_og_aud_større_enn_1() throws Exception {
        /*
         * JWT-body { "iss": "https://foo.bar.adeo.no/openam/oauth2", "exp": 1527256661,
         * "jti": "dLD-drhxM8xQUgI5eMAJTQ", "iat": 1527253061, "sub": "demo", "aud": [
         * "foo", "baz" ] }
         */
        String jwt = "eyJraWQiOiIxIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiJodHRwczovL2Zvby5iYXIuYWRlby5uby9vcGVuYW0vb2F1dGgyIiwiZXhwIjoxNTI3MjU2NjYxLCJqdGkiOiJkTEQtZHJoeE04eFFVZ0k1ZU1BSlRRIiwiaWF0IjoxNTI3MjUzMDYxLCJzdWIiOiJkZW1vIiwiYXVkIjpbImZvbyIsImJheiJdfQ.UkVLI7ae7o-_z7hde27q1eujCi_eLBjMS7_8NTMw2lNY4xVDBNOGp8LAKMvr41ykCMEqf8fxyI1jBSOOul3P8TODHX6ogrz9ig38qORINGynp8FJ1AbLtGFNyuN1BHJosj8wsGKqILUHGVJ7F2NWLJZ-Bu40AxS4566BC5WsPbQkHIfRr8YYqv6BBk0iirCcarNLW09vEgFN-hkEyPpIIMxuS6AjwNDIS0WbCSWGQ_5y5CzgoWAv5wgfLiOqdSqkx_RGgcIQNtUYtWkHWwvJBgIENAygpPtQAKd3DuJ7WOGR0RIp4AzkyTwlEnHBg3tgK0ieqhMfJdTgyXWWiQOYzg";

        var e = assertThrows(TekniskException.class, () -> JwtUtil.getClientName(JwtUtil.getClaims(jwt)));
        assertTrue(e.getMessage().contains("[foo, baz]"));
        assertTrue(e.getMessage().contains("Kan ikke utlede clientName"));

    }
}
