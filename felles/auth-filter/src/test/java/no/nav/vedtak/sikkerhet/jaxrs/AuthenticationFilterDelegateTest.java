package no.nav.vedtak.sikkerhet.jaxrs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorResult;

class AuthenticationFilterDelegateTest {

    private static final String SYSTEMBRUKER_PROP = "systembruker.username";

    private final OidcTokenValidator tokenValidator = Mockito.mock(OidcTokenValidator.class);

    private final ContainerRequestContext request = Mockito.mock(ContainerRequestContext.class);

    public void setupAll() {
        var wellKnownUrl = OidcTokenGenerator.ISSUER + "/dummy";
        System.setProperty(AzureProperty.AZURE_APP_WELL_KNOWN_URL.name(), wellKnownUrl);
        System.setProperty(AzureProperty.AZURE_APP_CLIENT_ID.name(), "OIDC");
        System.setProperty(AzureProperty.AZURE_APP_CLIENT_SECRET.name(), "dummy");
        System.setProperty(AzureProperty.AZURE_OPENID_CONFIG_ISSUER.name(), OidcTokenGenerator.ISSUER);
        System.setProperty(AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI.name(), OidcTokenGenerator.ISSUER + "/jwks_uri");
        System.setProperty(AzureProperty.AZURE_OPENID_CONFIG_TOKEN_ENDPOINT.name(), "dummy");

        System.setProperty(SYSTEMBRUKER_PROP, "JUnit Test");

        OidcTokenValidatorConfig.addValidator(OpenIDProvider.AZUREAD, tokenValidator);
    }

    @BeforeEach
    public void setUp() {
        setupAll();
    }

    @AfterEach
    public void teardown() {
        Arrays.asList(AzureProperty.values()).forEach(p -> System.clearProperty(p.name()));
        System.clearProperty(SYSTEMBRUKER_PROP);
    }

    @Test
    void skal_slippe_gjennom_forespørsel_etter_ubeskyttet_ressurs() throws Exception {
        Method method = RestClass.class.getMethod("ubeskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNull();
    }


    @Test
    void skal_ikke_slippe_gjennom_forespørsel_men_svare_med_401_etter_beskyttet_ressurs_når_forespørselen_ikke_har_med_id_token() throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        when(request.getHeaderString("Authorization")).thenReturn(null);

        assertThrows(WebApplicationException.class, () -> AuthenticationFilterDelegate.validerSettKontekst(ri, request));
        try {
            AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }
    }


    @Test
    void skal_sende_401_for_utløpt_Authorization_header() throws Exception {
        var utløptIdToken = getUtløptToken();
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + utløptIdToken.token());

        when(tokenValidator.validate(utløptIdToken)).thenReturn(OidcTokenValidatorResult.invalid("expired"));

        assertThrows(WebApplicationException.class, () -> AuthenticationFilterDelegate.validerSettKontekst(ri, request));
        try {
            AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }

    }

    @Test
    void skal_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_id_token_som_validerer() throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        var gyldigToken = getGyldigToken();

        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());

        when(tokenValidator.validate(gyldigToken)).thenReturn(
            OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + 121));

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNotNull();
    }


    @Test
    void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester() throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        int sekunderGjenståendeGyldigTid = 125;

        var gyldigToken = getGyldigToken();
        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());
        when(tokenValidator.validate(gyldigToken)).thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"),
            System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNotNull();
    }

    @Test
    void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester_selv_om_kortere_enn_gammel_grense() throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        int sekunderGjenståendeGyldigTid = 115;

        var gyldigToken = getGyldigToken();
        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());
        when(tokenValidator.validate(gyldigToken)).thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"),
            System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNotNull();
    }


    private static TokenString getGyldigToken() {
        return new OidcTokenGenerator().createCookieTokenHolder();
    }

    private static TokenString getUtløptToken() {
        return new OidcTokenGenerator().withExpiration(NumericDate.fromMilliseconds(System.currentTimeMillis() - 1)).createCookieTokenHolder();
    }

    @Path("foo")
    static class RestClass {

        @UtenAutentisering
        @Path("ubeskyttet")
        public void ubeskyttet() {
        }

        @Path("beskyttet")
        public void beskyttet() {
        }

    }

    private static class TestInvocationContext implements ResourceInfo {

        private final Method method;
        private final Class<?> resourceClass;

        TestInvocationContext(Method method, Class<?> resourceClass) {
            this.method = method;
            this.resourceClass = resourceClass;
        }

        @Override
        public Method getResourceMethod() {
            return method;
        }

        @Override
        public Class<?> getResourceClass() {
            return resourceClass;
        }

    }

}
