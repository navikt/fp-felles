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
    private FnrDto fnr1 = new FnrDto("00000000000");
    private BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    @Test
    public void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton(fnr1.getFnr()));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        });

        Method method = RestClass.class.getMethod("fnrIn", FnrDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{fnr1});
        interceptor.wrapTransaction(ic);

        sniffer.assertHasInfoMessage("action=/foo/fnr_in abac_action=create abac_resource_type=no.nav.abac.attributter.foreldrepenger.fagsak fnr=00000000000");
    }

    @Test
    public void skal_også_logge_input_parametre_til_sporingslogg_ved_permit() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(fnr1.getFnr())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        });

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);

        sniffer.assertHasInfoMessage("action=/foo/behandling_id_in abac_action=create abac_resource_type=no.nav.abac.attributter.foreldrepenger.fagsak behandlingId=1234 fnr=00000000000");
    }

    @Test
    public void skal_ikke_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_permit_når_det_er_konfigurert_unntak_i_annotering() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(fnr1.getFnr())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.GODKJENT,
                Collections.singletonList(Decision.Permit),
                pdpRequest);
        });

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);

        assertThat(sniffer.countEntries("action")).isZero();
    }

    @Test
    public void skal_logge_parametre_som_går_til_pdp_til_sporingslogg_ved_deny() throws Exception {
        BeskyttetRessursInterceptor interceptor = new BeskyttetRessursInterceptor(attributter -> {
            PdpRequest pdpRequest = new PdpRequest();
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, (Collections.singleton(fnr1.getFnr())));
            pdpRequest.put(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
            pdpRequest.put(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
            return new Tilgangsbeslutning(
                AbacResultat.AVSLÅTT_KODE_6,
                Collections.singletonList(Decision.Deny),
                pdpRequest);
        });

        Method method = RestClass.class.getMethod("fnrIn", FnrDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{fnr1});

        try {
            interceptor.wrapTransaction(ic);
            Fail.fail("Skal få exception");
        } catch (ManglerTilgangException e) {
            //FORVENTET
        }
        sniffer.assertHasInfoMessage("action=/foo/fnr_in abac_action=create abac_resource_type=no.nav.abac.attributter.foreldrepenger.fagsak decision=Deny fnr=00000000000");
    }

    @Path("foo")
    public static class RestClass {

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
        @Path("fnr_in")
        public void fnrIn(@SuppressWarnings("unused") FnrDto param) {

        }

        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK)
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {

        }


        @BeskyttetRessurs(action = BeskyttetRessursActionAttributt.CREATE, ressurs = BeskyttetRessursResourceAttributt.FAGSAK, sporingslogg = false)
        @Path("uten_sporingslogg")
        public void utenSporingslogg(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

    }

    private static class FnrDto implements AbacDto {

        private String fnr;

        public FnrDto(String fnr) {
            this.fnr = fnr;
        }

        public String getFnr() {
            return fnr;
        }


        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTilFødselsnummer(fnr);
        }
    }

    private static class BehandlingIdDto implements AbacDto {

        private Long behandlingId;

        public BehandlingIdDto(Long behandlingId) {
            this.behandlingId = behandlingId;
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTilBehandlingsId(behandlingId);
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
