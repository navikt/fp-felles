package no.nav.vedtak.sikkerhet.pdp;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
import static no.nav.vedtak.util.env.Environment.NAIS_APP_NAME;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.pdp.xacml.Advice;
import no.nav.vedtak.sikkerhet.pdp.xacml.BiasedDecisionResponse;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class PdpKlientImpl implements PdpKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(PdpKlientImpl.class);
    private XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste;

    private PdpConsumer pdp;

    public PdpKlientImpl() {
    }

    @Inject
    public PdpKlientImpl(PdpConsumer pdp, XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste) {
        this.pdp = pdp;
        this.xamlRequestBuilderTjeneste = xamlRequestBuilderTjeneste;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest req) {
        var builder = xamlRequestBuilderTjeneste.lagXacmlRequestBuilder(req);
        leggPåTokenInformasjon(builder, req);
        var response = pdp.evaluate(builder);
        var hovedresultat = resultatFraResponse(evaluateWithBias(response));
        return new Tilgangsbeslutning(hovedresultat, response.getDecisions(), req);
    }

    static void leggPåTokenInformasjon(XacmlRequestBuilder builder, PdpRequest req) {
        var attrs = new XacmlAttributeSet();
        attrs.addAttribute(ENVIRONMENT_FELLES_PEP_ID, getPepId());
        var idToken = AbacIdToken.class.cast(req.get(ENVIRONMENT_AUTH_TOKEN));
        switch (idToken.getTokenType()) {
            case OIDC:
                String key = ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
                LOG.trace("Legger ved token med type oidc på {}", key);
                attrs.addAttribute(key, JwtUtil.getJwtBody(idToken.getToken()));
                break;
            case TOKENX:
                String keyX = ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
                LOG.trace("Legger IKKE ved token med type tokenX på {}", keyX);
                // attrs.addAttribute(keyX, JwtUtil.getJwtBody(idToken.getToken()));
                break;
            case SAML:
                LOG.trace("Legger på token med type saml");
                attrs.addAttribute(ENVIRONMENT_FELLES_SAML_TOKEN, base64encode(idToken.getToken()));
                break;
        }

        builder.addEnvironmentAttributeSet(attrs);
    }

    private static String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private static AbacResultat resultatFraResponse(BiasedDecisionResponse response) {
        if (response.getBiasedDecision() == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }
        var denyAdvice = response.getXacmlResponse().getAdvice();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: " + LoggerUtils.toStringWithoutLineBreaks(denyAdvice));
        }
        if (denyAdvice.contains(Advice.DENY_KODE_6)) {
            return AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvice.contains(Advice.DENY_KODE_7)) {
            return AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvice.contains(Advice.DENY_EGEN_ANSATT)) {
            return AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static BiasedDecisionResponse evaluateWithBias(XacmlResponseWrapper response) {
        var decisions = response.getDecisions();

        for (var decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw new TekniskException("F-080281",
                        String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", decision, response));
            }
        }

        var biasedDecision = createAggregatedDecision(decisions);
        var decisionResponse = new BiasedDecisionResponse(biasedDecision, response);
        handlObligation(decisionResponse);
        return decisionResponse;
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        for (var decision : decisions) {
            if (decision != Decision.Permit)
                return Decision.Deny;
        }
        return Decision.Permit;
    }

    private static void handlObligation(BiasedDecisionResponse response) {
        var obligations = response.getXacmlResponse().getObligations();
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }

    private static String getPepId() {
        return ENV.getProperty(NAIS_APP_NAME, "local-app");
    }
}
