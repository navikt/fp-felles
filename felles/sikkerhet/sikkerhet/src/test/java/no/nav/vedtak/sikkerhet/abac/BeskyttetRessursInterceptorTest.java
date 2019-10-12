package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;

import org.assertj.core.api.Fail;
import org.junit.Rule;
import org.junit.Test;

import no.nav.abac.common.xacml.CommonAttributter;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.sikkerhet.InnloggetSubject;

public class BeskyttetRessursInterceptorTest {

    private final RestClass tjeneste = new RestClass();
    @Rule
    public InnloggetSubject innloggetSubject = new InnloggetSubject().medOidcToken("dummy.oidc.token");
    @Rule
    public LogSniffer sniffer = new LogSniffer();
    private AktørDto aktør1 = new AktørDto("00000000000");
    private BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    @Test
    public void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, Collections.singleton(aktør1.getAktørId()));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, new LegacyAbacSporingslogg());

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });
        interceptor.wrapTransaction(ic);

        sniffer.assertHasInfoMessage("action=/foo/aktoer_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000");
    }

    @Test
    public void skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, new LegacyAbacSporingslogg());

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);

        sniffer.assertHasInfoMessage(
            "action=/foo/behandling_id_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000 behandlingId=1234");
    }

    @Test
    public void skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        }, new LegacyAbacSporingslogg());

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { behandlingIdDto });
        interceptor.wrapTransaction(ic);

        assertThat(sniffer.countEntries("action")).isZero();
    }

    @Test
    public void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(aktør1.getAktørId())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.AVSLÅTT_KODE_6,
                Collections.singletonList(Decision.Deny),
                pdpRequest);
        }, new LegacyAbacSporingslogg());

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[] { aktør1 });

        try {
            interceptor.wrapTransaction(ic);
            Fail.fail("Skal få exception");
        } catch (ManglerTilgangException e) {
            // FORVENTET
        }
        sniffer.assertHasInfoMessage(
            "action=/foo/aktoer_in abac_action=create abac_resource_type=pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker aktorId=00000000000 decision=Deny");
    }

    @Path("foo")
    public static class RestClass {

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.PIP)
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {

        }

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.PIP)
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.PIP, sporingslogg = false)
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
