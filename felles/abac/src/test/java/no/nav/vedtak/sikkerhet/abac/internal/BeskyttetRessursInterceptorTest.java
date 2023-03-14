package no.nav.vedtak.sikkerhet.abac.internal;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.sikkerhet.abac.*;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import static no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter.RESOURCE_TYPE_INTERNAL_PIP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeskyttetRessursInterceptorTest {

    private static final String DUMMY_ID_TOKEN = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";
    private final RestClass tjeneste = new RestClass();

    private final AktørDto aktør1 = new AktørDto("00000000000");
    private final BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    private final ArgumentCaptor<Auditdata> auditdataCaptor = ArgumentCaptor.forClass(Auditdata.class);

    public static final OpenIDToken DUMMY_OPENID_TOKEN = new OpenIDToken(OpenIDProvider.STS, new TokenString(DUMMY_ID_TOKEN));

    @Mock
    private static TokenProvider tokenProvider;


    private void mockTokenProvider() {
        when(tokenProvider.getUid()).thenReturn("A000000");
        when(tokenProvider.openIdToken()).thenReturn(DUMMY_OPENID_TOKEN);
    }

    @Test
    void auditlogg_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit(abacAuditlogger);
        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit(AbacAuditlogger abacAuditLogger) throws Exception {
        mockTokenProvider();

        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            var ressurs = AppRessursData.builder().leggTilAktørId(aktør1.getAktørId()).build();
            return new Tilgangsbeslutning(AbacResultat.GODKJENT, attributter, ressurs);
        }, abacAuditLogger, tokenProvider);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{aktør1});
        interceptor.wrapTransaction(ic);
    }

    @Test
    void auditlogg_skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_også_logge_input_parametre_til_sporingslogg_ved_permit(abacAuditlogger);
        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ flexString2=1234 flexString2Label=Behandling request=/foo/behandling_id_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_også_logge_input_parametre_til_sporingslogg_ved_permit(AbacAuditlogger abacAuditLogger) throws Exception {
        mockTokenProvider();

        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            var ressurs = AppRessursData.builder().leggTilAktørId(aktør1.getAktørId()).build();
            return new Tilgangsbeslutning(AbacResultat.GODKJENT, attributter, ressurs);
        }, abacAuditLogger, tokenProvider);

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);
    }

    @Test
    void auditlog_skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering() throws Exception {
        when(tokenProvider.openIdToken()).thenReturn(DUMMY_OPENID_TOKEN);
        final Auditlogger auditlogger = mock(Auditlogger.class);
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering(abacAuditlogger);
        verify(auditlogger, never()).logg(Mockito.any());
    }

    private void skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering(AbacAuditlogger abacAuditLogger) throws Exception {
        mockTokenProvider();
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            var ressurs = AppRessursData.builder().leggTilAktørId(aktør1.getAktørId()).build();
            return new Tilgangsbeslutning(AbacResultat.GODKJENT, attributter, ressurs);
        }, abacAuditLogger, tokenProvider);

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);
    }

    @Test
    void auditlog_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        mockTokenProvider();
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny(abacAuditlogger);
        assertGotPattern(auditlogger,
            "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|WARN|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny(AbacAuditlogger abacAuditLogger) throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            var ressurs = AppRessursData.builder().leggTilAktørId(aktør1.getAktørId()).build();
            return new Tilgangsbeslutning(AbacResultat.AVSLÅTT_KODE_6, attributter, ressurs);
        }, abacAuditLogger, tokenProvider);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{aktør1});

        try {
            interceptor.wrapTransaction(ic);
            Fail.fail("Skal få exception");
        } catch (ManglerTilgangException e) {
            // FORVENTET
        }
    }

    private void assertGotPattern(final Auditlogger auditlogger, String expected) {
        verify(auditlogger).logg(auditdataCaptor.capture());
        final String actual = auditdataCaptor.getValue().toString();
        assertThat(actual).matches(toAuditdataPattern(expected));
    }

    private static Auditlogger mockAuditLogger() {
        final Auditlogger auditlogger = mock(Auditlogger.class);
        when(auditlogger.getDefaultVendor()).thenReturn("felles");
        when(auditlogger.getDefaultProduct()).thenReturn("felles-test");
        return auditlogger;
    }

    private static final String toAuditdataPattern(String s) {
        return Pattern.quote(s).replaceAll("__NUMBERS__", unquoteInReplacement("[0-9]*"));
    }

    private static final String unquoteInReplacement(String s) {
        return "\\\\E" + s + "\\\\Q";
    }

    @Path("foo")
    static class RestClass {

        @BeskyttetRessurs(actionType = ActionType.CREATE, resource = RESOURCE_TYPE_INTERNAL_PIP)
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {

        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resource = RESOURCE_TYPE_INTERNAL_PIP)
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resource = RESOURCE_TYPE_INTERNAL_PIP, sporingslogg = false)
        @Path("uten_sporingslogg")
        public void utenSporingslogg(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

    }

    private static class AktørDto implements AbacDto {

        private String aktørId;

        public AktørDto(String aktørId) {
            this.aktørId = aktørId;
        }

        public String getAktørId() {
            return aktørId;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, aktørId);
        }
    }

    private static class BehandlingIdDto implements AbacDto {

        private Long id;

        public BehandlingIdDto(Long id) {
            this.id = id;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, id);
        }
    }

    private class TestInvocationContext implements InvocationContext {

        private Method method;
        private Object[] parameters;

        TestInvocationContext(Method method, Object[] parameters) {
            this.method = method;
            this.parameters = parameters;
        }

        @Override
        public Object getTarget() {
            return tjeneste;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return parameters;
        }

        @Override
        public void setParameters(Object[] params) {
            parameters = params;
        }

        @Override
        public Map<String, Object> getContextData() {
            return null;
        }

        @Override
        public Object proceed() throws Exception {
            return method.invoke(tjeneste, parameters);
        }
    }
}
