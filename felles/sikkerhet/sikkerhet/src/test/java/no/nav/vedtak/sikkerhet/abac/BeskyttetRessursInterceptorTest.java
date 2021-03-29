package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.log.audit.Auditdata;
import no.nav.vedtak.log.audit.Auditlogger;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.sikkerhet.InnloggetSubjectExtension;
import no.nav.vedtak.util.AppLoggerFactory;

@ExtendWith(InnloggetSubjectExtension.class)
public class BeskyttetRessursInterceptorTest {

    private final RestClass tjeneste = new RestClass();

    private AktørDto aktør1 = new AktørDto("00000000000");
    private BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    private final ArgumentCaptor<Auditdata> auditdataCaptor = ArgumentCaptor.forClass(Auditdata.class);

    private AbacAuditlogger noAuditLogger = new AbacAuditlogger(new Auditlogger(true, "felles", "felles-test"));

    private static MemoryAppender sniffer;
    private static Logger LOG;

    @BeforeAll
    public static void beforeAll() {
        LOG = Logger.class.cast(AppLoggerFactory.getSporingLogger(DefaultAbacSporingslogg.class));
        LOG.setLevel(Level.INFO);
        sniffer = new MemoryAppender(LOG.getName());
        LOG.addAppender(sniffer);
        sniffer.start();
    }

    @AfterEach
    public void afterEach() {
        sniffer.reset();
    }

    @Test
    public void sporingslogg_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit(noAuditLogger);
        assertLogged(
                "action=/foo/aktoer_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000");
    }

    @Test
    public void auditlogg_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);

        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit(abacAuditlogger);
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit(AbacAuditlogger abacAuditLogger)
            throws NoSuchMethodException, Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, Collections.singleton(aktør1.getAktørId()));
            pdpRequest.put(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                    pdpRequest);
        }, new DefaultAbacSporingslogg(), abacAuditLogger, new TokenProvider() {
        });

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });
        interceptor.wrapTransaction(ic);
    }

    @Test
    public void sporingslogg_skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        skal_også_logge_input_parametre_til_sporingslogg_ved_permit(noAuditLogger);
        assertLogged(
                "action=/foo/behandling_id_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000 behandlingId=1234");
    }

    @Test
    public void auditlogg_skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_også_logge_input_parametre_til_sporingslogg_ved_permit(abacAuditlogger);
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|INFO|act=create duid=00000000000 end=__NUMBERS__ flexString2=1234 flexString2Label=Behandling request=/foo/behandling_id_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_også_logge_input_parametre_til_sporingslogg_ved_permit(AbacAuditlogger abacAuditLogger) throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                    pdpRequest);
        }, new DefaultAbacSporingslogg(), abacAuditLogger, new TokenProvider() {
        });

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);
    }

    @Test
    public void sporingslogg_skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering()
            throws Exception {
        skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering(noAuditLogger);
        assertThat(sniffer.countEntries("action")).isZero();
    }

    @Test
    public void auditlog_skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering()
            throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering(abacAuditlogger);
        verify(auditlogger, never()).logg(Mockito.any());
    }

    private void skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering(
            AbacAuditlogger abacAuditLogger) throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                    AbacResultat.GODKJENT,
                    Collections.singletonList(Decision.Permit),
                    pdpRequest);
        }, new DefaultAbacSporingslogg(), abacAuditLogger, new TokenProvider() {
        });

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);
    }

    @Test
    public void sporingslog_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny(noAuditLogger);
        assertLogged(
                "action=/foo/aktoer_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000 decision=Deny");
    }

    @Test
    public void auditlog_skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        final Auditlogger auditlogger = mockAuditLogger();
        final AbacAuditlogger abacAuditlogger = new AbacAuditlogger(auditlogger);
        skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny(abacAuditlogger);
        assertGotPattern(auditlogger,
                "CEF:0|felles|felles-test|1.0|audit:create|ABAC Sporingslogg|WARN|act=create duid=00000000000 end=__NUMBERS__ request=/foo/aktoer_in requestContext=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker suid=A000000");
    }

    private void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny(AbacAuditlogger abacAuditLogger) throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                    AbacResultat.AVSLÅTT_KODE_6,
                    Collections.singletonList(Decision.Deny),
                    pdpRequest);
        }, new DefaultAbacSporingslogg(), abacAuditLogger, new TokenProvider() {
        });

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });

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
        when(auditlogger.isEnabled()).thenReturn(true);
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
    public static class RestClass {

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {

        }

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker")
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, resource = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker", sporingslogg = false)
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

    private static void assertLogged(String string) {
        assertThat(sniffer.searchInfo(string)).isNotNull();
    }
}
