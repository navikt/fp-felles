package no.nav.vedtak.sikkerhet.jaxrs;

import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.ÅpenRessurs;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorResult;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.security.auth.Subject;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AuthenticationFilterDelegateTest {

    private OidcTokenValidator tokenValidator = Mockito.mock(OidcTokenValidator.class);

    private ContainerRequestContext request = Mockito.mock(ContainerRequestContext.class);

    private Subject subject = new Subject();
    private Subject serviceSubject = new Subject();

    public void setupAll() throws Exception {

        System.setProperty(AzureProperty.AZURE_APP_WELL_KNOWN_URL.name(), OidcTokenGenerator.ISSUER + "/" + WellKnownConfigurationHelper.STANDARD_WELL_KNOWN_PATH);
        System.setProperty(AzureProperty.AZURE_APP_CLIENT_ID.name(), "OIDC");
        System.setProperty(AzureProperty.AZURE_OPENID_CONFIG_ISSUER.name(), OidcTokenGenerator.ISSUER);
        System.setProperty(AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI.name(), OidcTokenGenerator.ISSUER + "/jwks_uri");
        System.setProperty("systembruker.username", "JUnit Test");

        Map<String, String> testData = Map.of(
            "issuer", OidcTokenGenerator.ISSUER,
            AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI.name(), OidcTokenGenerator.ISSUER + "/jwks_uri"
        );
        WellKnownConfigurationHelper.setWellKnownConfig(OidcTokenGenerator.ISSUER + "/" + WellKnownConfigurationHelper.STANDARD_WELL_KNOWN_PATH, JsonUtil.toJson(testData));
        OidcTokenValidatorConfig.addValidator(OpenIDProvider.AZUREAD, tokenValidator);
    }

    @BeforeEach
    public void setUp() throws Exception {
        WellKnownConfigurationHelper.unsetWellKnownConfig();
        setupAll();
    }

    @AfterEach
    public void teardown() throws Exception {
        System.clearProperty(AzureProperty.AZURE_APP_WELL_KNOWN_URL.name());
        System.clearProperty(AzureProperty.AZURE_APP_CLIENT_ID.name());
        System.clearProperty(AzureProperty.AZURE_OPENID_CONFIG_ISSUER.name());
        System.clearProperty(AzureProperty.AZURE_OPENID_CONFIG_JWKS_URI.name());
        System.clearProperty("systembruker.username");

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
    }


    @Test
    void skal_sende_401_for_utløpt_Authorization_header() throws Exception {
        var utløptIdToken = getUtløptToken();
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + utløptIdToken);

        when(tokenValidator.validate(utløptIdToken))
            .thenReturn(OidcTokenValidatorResult.invalid("expired"));

        assertThrows(WebApplicationException.class, () -> AuthenticationFilterDelegate.validerSettKontekst(ri, request));

    }

    @Test
    void skal_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_id_token_som_validerer()
        throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        var gyldigToken = getGyldigToken();

        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());

        when(tokenValidator.validate(gyldigToken))
            .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + 121));

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNotNull();
    }


    @Test
    void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester()
        throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        int sekunderGjenståendeGyldigTid = 125;

        var gyldigToken = getGyldigToken();
        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());
        when(tokenValidator.validate(gyldigToken))
            .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        AuthenticationFilterDelegate.validerSettKontekst(ri, request);
        assertThat(KontekstHolder.getKontekst().getContext()).isEqualTo(SikkerhetContext.REQUEST);
        assertThat(KontekstHolder.getKontekst().getUid()).isNotNull();
    }

    @Test
    void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester_selv_om_kortere_enn_gammel_grense()
        throws Exception {
        Method method = RestClass.class.getMethod("beskyttet");
        ResourceInfo ri = new TestInvocationContext(method, RestClass.class);

        int sekunderGjenståendeGyldigTid = 115;

        var gyldigToken = getGyldigToken();
        when(request.getHeaderString("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + gyldigToken.token());
        when(tokenValidator.validate(gyldigToken))
            .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

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

        @ÅpenRessurs
        @Path("ubeskyttet")
        public void ubeskyttet() {
        }

        @BeskyttetRessurs()
        @Path("beskyttet")
        public void beskyttet() {
        }

    }

    private class TestInvocationContext implements ResourceInfo {

        private Method method;
        private Class<?> resourceClass;

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
