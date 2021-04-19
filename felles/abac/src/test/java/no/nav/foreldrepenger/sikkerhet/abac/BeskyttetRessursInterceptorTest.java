package no.nav.foreldrepenger.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.interceptor.InvocationContext;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.sikkerhet.abac.auditlog.AbacAuditlogger;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacDataAttributter;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.StandardAbacAttributtType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.Auditlogger;

class BeskyttetRessursInterceptorTest {

    private static final String DUMMY_ID_TOKEN = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";
    private final RestClass tjeneste = new RestClass();

    private final AktørDto aktør1 = new AktørDto("12345678901");
    private final BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    private final ArgumentCaptor<Auditdata> auditdataCaptor = ArgumentCaptor.forClass(Auditdata.class);

    @Test
    void auditlogg_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = lagPdpRequest(attributter);
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, abacAuditlogger);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });
        interceptor.wrapTransaction(ic);
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=12345678901 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    @Test
    void auditlogg_skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = lagPdpRequest(attributter);
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, abacAuditlogger);

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=12345678901 end=__NUMBERS__ flexString2=1234 flexString2Label=Behandling request=/foo/behandling_id_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    @Test
    void auditlog_skal_ikke_logge_parametre_som_går_til_pdp_til_auditlogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering()
            throws Exception {

        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = lagPdpRequest(attributter);
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, abacAuditlogger);

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);
        verify(auditlogger, never()).logg(Mockito.any());
    }

    @Test
    void auditlog_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = lagPdpRequest(attributter);
            return new Tilgangsbeslutning(
                    AbacResultat.AVSLÅTT_KODE_6,
                    Collections.singletonList(Decision.Deny),
                pdpRequest);
        }, abacAuditlogger);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });

        try {
            interceptor.wrapTransaction(ic);
            Fail.fail("Skal få exception");
        } catch (ManglerTilgangException e) {
            // FORVENTET
        }
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|WARN|act=create duid=12345678901 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private PdpRequest lagPdpRequest(final BeskyttRessursAttributer attributter) {
        PdpRequest pdpRequest = PdpRequest.builder()
            .medActionType(attributter.getActionType())
            .medResourceType(attributter.getResource())
            .medIdToken(IdToken.withToken(DUMMY_ID_TOKEN, TokenType.OIDC))
            .medUserId("A000000")
            .medRequest(attributter.getRequestPath())
            .medDomene("foreldrepenger")
            .build();
         pdpRequest.setAktørIder(Set.of(aktør1.getAktørId()));
        return pdpRequest;
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

    private static String toAuditdataPattern(String s) {
        return Pattern.quote(s).replaceAll("__NUMBERS__", unquoteInReplacement("[0-9]*"));
    }

    private static String unquoteInReplacement(String s) {
        return "\\\\E" + s + "\\\\Q";
    }

    static class RestClass {

        @BeskyttetRessurs(action = ActionType.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker", path = "/foo/aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) { }

        @BeskyttetRessurs(action = ActionType.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker", path = "/foo/behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) { }

        @BeskyttetRessurs(action = ActionType.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker", path = "/foo/uten_sporingslogg", sporingslogg = false)
        public void utenSporingslogg(@SuppressWarnings("unused") BehandlingIdDto param) { }
    }

    private static class AktørDto implements AbacDto {

        private final String aktørId;

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

        private final Long id;

        public BehandlingIdDto(Long id) {
            this.id = id;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, id);
        }
    }

    private class TestInvocationContext implements InvocationContext {

        private final Method method;
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
