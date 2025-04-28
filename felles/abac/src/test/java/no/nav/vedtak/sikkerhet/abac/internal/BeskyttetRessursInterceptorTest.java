package no.nav.vedtak.sikkerhet.abac.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursInterceptor;
import no.nav.vedtak.sikkerhet.abac.Pep;
import no.nav.vedtak.sikkerhet.abac.PepNektetTilgangException;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

@ExtendWith(MockitoExtension.class)
class BeskyttetRessursInterceptorTest {

    private final RestClass tjeneste = new RestClass();

    private final AktørDto aktør1 = new AktørDto("00000000000");
    private final BehandlingIdDto behandlingIdDto = new BehandlingIdDto(UUID.randomUUID());

    private static final String BRUKER_IDENT = "A000000";
    private static final UUID BRUKER_OID = UUID.randomUUID();

    private final ArgumentCaptor<BeskyttetRessursAttributter> braCaptor = ArgumentCaptor.forClass(BeskyttetRessursAttributter.class);

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private Pep pep;


    @BeforeEach
    void mockTokenProvider() {
        when(tokenProvider.getUid()).thenReturn(BRUKER_IDENT);
        when(tokenProvider.getOid()).thenReturn(BRUKER_OID);
        when(tokenProvider.getAnsattGrupper()).thenReturn(Set.of());
        when(tokenProvider.getIdentType()).thenReturn(IdentType.InternBruker);
    }

    @Test
    void godkjent_aktør() throws Exception {
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(TilgangResultat.GODKJENT);
        var interceptor = new BeskyttetRessursInterceptor(pep, tokenProvider);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{aktør1});
        interceptor.wrapTransaction(ic);

        var bra = braCaptor.getValue();
        assertBeskyttetRessursAttributter(bra);
        assertThat(bra.isSporingslogg()).isTrue();
    }

    @Test
    void godkjent_behandling() throws Exception {
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(TilgangResultat.GODKJENT);
        var interceptor = new BeskyttetRessursInterceptor(pep, tokenProvider);

        Method method = RestClass.class.getMethod("behandlingIdIn", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);

        var bra = braCaptor.getValue();
        assertBeskyttetRessursAttributter(bra);
        assertThat(bra.isSporingslogg()).isTrue();
    }


    @Test
    void godkjent_uten_sporing() throws Exception {
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(TilgangResultat.GODKJENT);
        var interceptor = new BeskyttetRessursInterceptor(pep, tokenProvider);

        Method method = RestClass.class.getMethod("utenSporingslogg", BehandlingIdDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{behandlingIdDto});
        interceptor.wrapTransaction(ic);

        var bra = braCaptor.getValue();
        assertBeskyttetRessursAttributter(bra);
        assertThat(bra.isSporingslogg()).isFalse();
    }

    @Test
    void deny_aktør_gir_exception() throws Exception {
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(TilgangResultat.AVSLÅTT_KODE_6);
        var interceptor = new BeskyttetRessursInterceptor(pep, tokenProvider);

        Method method = RestClass.class.getMethod("aktoerIn", AktørDto.class);
        InvocationContext ic = new TestInvocationContext(method, new Object[]{aktør1});
        Assertions.assertThrowsExactly(PepNektetTilgangException.class, () -> interceptor.wrapTransaction(ic), () -> "F-709170:Tilgangskontroll.Avslag.Kode6");

        var bra = braCaptor.getValue();
        assertBeskyttetRessursAttributter(bra);
        assertThat(bra.isSporingslogg()).isTrue();
    }

    private void assertBeskyttetRessursAttributter(BeskyttetRessursAttributter bra) {
        assertThat(bra.getBrukerId()).isEqualTo(BRUKER_IDENT);
        assertThat(bra.getBrukerOid()).isEqualTo(BRUKER_OID);
        assertThat(bra.getIdentType()).isEqualTo(IdentType.InternBruker);
        assertThat(bra.getAnsattGrupper()).isEmpty();
        assertThat(bra.getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(bra.getResourceType()).isEqualTo(ResourceType.PIP);
        assertThat(bra.getAvailabilityType()).isEqualTo(AvailabilityType.INTERNAL);
        assertThat(bra.getServicePath()).startsWith("/foo");
        assertThat(bra.getDataAttributter().keySet()).hasSize(1);
    }

    @Path("foo")
    static class RestClass {

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.PIP, sporingslogg = true)
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {

        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.PIP, sporingslogg = true)
        @Path("behandling_id_in")
        public void behandlingIdIn(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.PIP, sporingslogg = false)
        @Path("uten_sporingslogg")
        public void utenSporingslogg(@SuppressWarnings("unused") BehandlingIdDto param) {

        }

    }

    private record AktørDto(String aktørId) implements AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, aktørId);
        }
    }

    private record BehandlingIdDto(UUID id) implements AbacDto {

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_UUID, id);
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
