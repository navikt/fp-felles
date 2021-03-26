package no.nav.vedtak.sikkerhet.pdp;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.vedtak.sikkerhet.pdp.xacml.Obligation;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class PdpKlientImpl implements PdpKlient {

    private static final Logger LOG = LoggerFactory.getLogger(PdpKlientImpl.class);
    private XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste;

    private PdpConsumer pdpConsumer;

    public PdpKlientImpl() {
        // for CDI
    }

    @Inject
    public PdpKlientImpl(PdpConsumer pdpConsumer, XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste) {
        this.pdpConsumer = pdpConsumer;
        this.xamlRequestBuilderTjeneste = xamlRequestBuilderTjeneste;
        LOG.info("builder er " + xamlRequestBuilderTjeneste.getClass());
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = xamlRequestBuilderTjeneste.lagXacmlRequestBuilder(pdpRequest);
        leggPåTokenInformasjon(xacmlBuilder, pdpRequest);
        XacmlResponseWrapper response = pdpConsumer.evaluate(xacmlBuilder);
        BiasedDecisionResponse biasedResponse = evaluateWithBias(response);
        AbacResultat hovedresultat = resultatFraResponse(biasedResponse);
        return new Tilgangsbeslutning(hovedresultat, response.getDecisions(), pdpRequest);
    }

    void leggPåTokenInformasjon(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest) {
        var attrs = new XacmlAttributeSet();
        attrs.addAttribute(ENVIRONMENT_FELLES_PEP_ID, getPepId());
        AbacIdToken idToken = (AbacIdToken) pdpRequest.get(ENVIRONMENT_AUTH_TOKEN);
        switch (idToken.getTokenType()) {
            case OIDC:
                String key = ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
                LOG.trace("Legger ved token med type oidc på {}", key);
                attrs.addAttribute(key, JwtUtil.getJwtBody(idToken.getToken()));
                break;
            case TOKENX:
                String keyX = ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
                LOG.trace("Legger IKKE ved token med type tokenX på {}", keyX);
                attrs.addAttribute(keyX, JwtUtil.getJwtBody(idToken.getToken()));
                break;
            case SAML:
                LOG.trace("Legger på token med type saml");
                attrs.addAttribute(ENVIRONMENT_FELLES_SAML_TOKEN, base64encode(idToken.getToken()));
                break;
        }

        xacmlBuilder.addEnvironmentAttributeSet(attrs);
    }

    private String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private AbacResultat resultatFraResponse(BiasedDecisionResponse response) {
        if (response.getBiasedDecision() == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }
        List<Advice> denyAdvice = response.getXacmlResponse().getAdvice();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: " + LoggerUtils.toStringWithoutLineBreaks(denyAdvice)); // NOSONAR
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

    private BiasedDecisionResponse evaluateWithBias(XacmlResponseWrapper response) {
        List<Decision> decisions = response.getDecisions();

        for (Decision decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw PdpFeil.indeterminateDecisionFeil(decision, response);
            }
        }

        Decision biasedDecision = createAggregatedDecision(decisions);
        BiasedDecisionResponse decisionResponse = new BiasedDecisionResponse(biasedDecision, response);
        handlObligation(decisionResponse);
        return decisionResponse;
    }

    private Decision createAggregatedDecision(List<Decision> decisions) {
        for (Decision decision : decisions) {
            if (decision != Decision.Permit)
                return Decision.Deny;
        }
        return Decision.Permit;
    }

    private void handlObligation(BiasedDecisionResponse response) {
        List<Obligation> obligations = response.getXacmlResponse().getObligations();
        if (!obligations.isEmpty()) {
            throw PdpFeil.ukjentObligationsFeil(obligations);
        }
    }

    private static String getPepId() {
        return Environment.current().getProperty("NAIS_APP_NAME", "local-app");
    }
}
