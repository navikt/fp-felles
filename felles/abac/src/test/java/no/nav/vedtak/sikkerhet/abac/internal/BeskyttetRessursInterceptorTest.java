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
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
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
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

@ExtendWith(MockitoExtension.class)
public class BeskyttetRessursInterceptorTest {

    private static final String DUMMY_ID_TOKEN = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";
    private final RestClass tjeneste = new RestClass();

    private final AktørDto aktør1 = new AktørDto("00000000000");
    private final BehandlingIdDto behandlingIdDto = new BehandlingIdDto(1234L);

    private static final String BRUKER_IDENT = "A000000";
    private static final UUID BRUKER_OID = UUID.randomUUID();
    private static final String PEP_ID = "test";

    private final ArgumentCaptor<BeskyttetRessursAttributter> braCaptor = ArgumentCaptor.forClass(BeskyttetRessursAttributter.class);

    public static final OpenIDToken DUMMY_OPENID_TOKEN = new OpenIDToken(OpenIDProvider.TOKENX, new TokenString(DUMMY_ID_TOKEN));

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
        when(tokenProvider.openIdToken()).thenReturn(DUMMY_OPENID_TOKEN);
        when(pep.pepId()).thenReturn(PEP_ID);
    }

    @Test
    void godkjent_aktør() throws Exception {
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(AbacResultat.GODKJENT);
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
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(AbacResultat.GODKJENT);
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
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(AbacResultat.GODKJENT);
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
        when(pep.vurderTilgang(braCaptor.capture())).thenReturn(AbacResultat.AVSLÅTT_KODE_6);
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
        assertThat(bra.getPepId()).isEqualTo(PEP_ID);
        assertThat(bra.getToken().getOpenIDProvider()).isEqualTo(OpenIDProvider.TOKENX);
        assertThat(bra.getServicePath()).startsWith("/foo");
        assertThat(bra.getDataAttributter().keySet()).hasSize(1);
    }

    @Path("foo")
    static class RestClass {

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.PIP)
        @Path("aktoer_in")
        public void aktoerIn(@SuppressWarnings("unused") AktørDto param) {

        }

        @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.PIP)
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

    private record BehandlingIdDto(Long id) implements AbacDto {

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
