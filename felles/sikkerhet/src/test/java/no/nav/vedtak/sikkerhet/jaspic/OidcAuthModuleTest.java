package no.nav.vedtak.sikkerhet.jaspic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorConfig;
import no.nav.vedtak.sikkerhet.oidc.validator.OidcTokenValidatorResult;

public class OidcAuthModuleTest {

    private OidcTokenValidator tokenValidator = Mockito.mock(OidcTokenValidator.class);
    private TokenLocator tokenLocator = Mockito.mock(TokenLocator.class);
    private CallbackHandler callbackHandler = Mockito.mock(CallbackHandler.class);
    private final Configuration configuration = new LoginContextConfiguration();

    private OidcAuthModule authModule = new OidcAuthModule(tokenLocator, configuration, Mockito.mock(DelegatedProtectedResource.class));
    private HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    private Subject subject = new Subject();
    private Subject serviceSubject = new Subject();

    public void setupAll() throws Exception {
        authModule.initialize(null, null, callbackHandler, null);

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
    public void setUp() throws Exception{
        WellKnownConfigurationHelper.unsetWellKnownConfig();
        setupAll();
    }

    @Test
    public void skal_slippe_gjennom_forespørsel_etter_ubeskyttet_ressurs() throws Exception {
        MessageInfo request = createRequestForUnprotectedResource();

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_men_svare_med_401_etter_beskyttet_ressurs_når_forespørselen_ikke_har_med_id_token()
            throws Exception {
        when(request.getHeader("Accept")).thenReturn("application/json");
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_sende_401_for_ugyldig_Authorization_header()
            throws Exception {
        var utløptIdToken = getUtløptToken();

        when(request.getHeader("Accept")).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE + utløptIdToken);
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(utløptIdToken));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_men_svare_med_redirect_til_openam_etter_beskyttet_ressurs_når_forespørselen_ikke_har_med_id_token()
            throws Exception {
        when(request.getHeader("Accept")).thenReturn("*/*");
        MessageInfo request = createRequestForProtectedResource();

        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.empty());

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
    }

    @Test
    public void skal_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_id_token_som_validerer()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        var gyldigIdToken = getGyldigToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + 121));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_utløpt_id_token_og_ikke_noe_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        var ugyldigToken = getUtløptToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigToken));
        when(tokenValidator.validate(any()))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(any()))
                .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 - 10));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Resource is protected, but id token is missing or invalid.");
        verifyNoMoreInteractions(response);
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_og_svare_med_redirect_når_det_ikke_er_satt_application_json() throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        var ugyldigToken = getUtløptToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigToken));
        when(tokenValidator.validate(any()))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(any()))
                .thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
    }

    @Test
    public void skal_ikke_slippe_gjennom_forespørsel_etter_beskyttet_ressurs_når_forespørselen_har_med_et_utløpt_id_token_og_ikke_klarer_å_hente_nytt_token_med_refresh_token()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        var ugyldigToken = getUtløptToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(ugyldigToken));
        when(tokenValidator.validate(ugyldigToken)).thenReturn(OidcTokenValidatorResult.invalid("Tokenet er ikke gyldig"));
        when(tokenValidator.validateWithoutExpirationTime(any()))
                .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 - 10));

        AuthStatus result = authModule.validateRequest(request, subject, serviceSubject);

        assertThat(result).isEqualTo(AuthStatus.SEND_CONTINUE);
        verify(response).sendError(401, "Resource is protected, but id token is missing or invalid.");
    }


    @Test
    public void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        int sekunderGjenståendeGyldigTid = 125;

        var gyldigIdToken = getGyldigToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        var result = authModule.validateRequest(request, subject, serviceSubject);

        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    @Test
    public void skal_slippe_gjennom_token_tilstrekkelig_levetid_til_å_brukes_til_kall_til_andre_tjenester_selv_om_kortere_enn_gammel_grense()
            throws Exception {
        MessageInfo request = createRequestForProtectedResource();

        int sekunderGjenståendeGyldigTid = 115;

        var gyldigIdToken = getGyldigToken();
        when(tokenLocator.getToken(any(HttpServletRequest.class))).thenReturn(Optional.of(gyldigIdToken));
        when(tokenValidator.validate(gyldigIdToken))
                .thenReturn(OidcTokenValidatorResult.valid("demo", IdentType.utledIdentType("demo"), System.currentTimeMillis() / 1000 + sekunderGjenståendeGyldigTid));

        var result = authModule.validateRequest(request, subject, serviceSubject);
        assertThat(result).isEqualTo(AuthStatus.SUCCESS);
    }

    private MessageInfo createRequestForProtectedResource() {
        return createRequestForResource(true);
    }

    private MessageInfo createRequestForUnprotectedResource() {
        return createRequestForResource(false);
    }

    private MessageInfo createRequestForResource(boolean isProtected) {
        MessageInfo messageInfo = Mockito.mock(MessageInfo.class);
        Map<Object, Object> properties = new HashMap<>();
        properties.put("javax.security.auth.message.MessagePolicy.isMandatory", Boolean.toString(isProtected));
        when(messageInfo.getMap()).thenReturn(properties);
        when(messageInfo.getRequestMessage()).thenReturn(request);
        when(messageInfo.getResponseMessage()).thenReturn(response);
        return messageInfo;
    }

    private static TokenString getGyldigToken() {
        return new OidcTokenGenerator().createCookieTokenHolder();
    }

    private static TokenString getUtløptToken() {
        return new OidcTokenGenerator().withExpiration(NumericDate.fromMilliseconds(System.currentTimeMillis() - 1)).createCookieTokenHolder();
    }

}
