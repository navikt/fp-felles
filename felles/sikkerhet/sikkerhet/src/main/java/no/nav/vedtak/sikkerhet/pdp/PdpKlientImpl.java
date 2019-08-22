package no.nav.vedtak.sikkerhet.pdp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abac.xacml.CommonAttributter;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.pdp.feil.PdpFeil;
import no.nav.vedtak.sikkerhet.pdp.xacml.Advice;
import no.nav.vedtak.sikkerhet.pdp.xacml.BiasedDecisionResponse;
import no.nav.vedtak.sikkerhet.pdp.xacml.Obligation;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

@ApplicationScoped
public class PdpKlientImpl implements PdpKlient {

    private static final Logger logger = LoggerFactory.getLogger(PdpKlientImpl.class);
    private XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste;
    private String pepId;

    private PdpConsumer pdpConsumer;

    public PdpKlientImpl(){
        //for CDI
    }

    @Inject
    public PdpKlientImpl(PdpConsumer pdpConsumer, XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste,
                         @KonfigVerdi("systembruker.username") String pepId) {
        this.pdpConsumer = pdpConsumer;
        this.xamlRequestBuilderTjeneste = xamlRequestBuilderTjeneste;
        this.pepId = pepId;
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
        XacmlAttributeSet environmentAttributeSet = new XacmlAttributeSet();
        environmentAttributeSet.addAttribute(CommonAttributter.ENVIRONMENT_FELLES_PEP_ID, pepId);
        AbacIdToken idToken = (AbacIdToken) pdpRequest.get(ENVIRONMENT_AUTH_TOKEN);
        if (idToken.erOidcToken()) {
            environmentAttributeSet.addAttribute(CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, JwtUtil.getJwtBody(idToken.getToken()));
        } else {
            environmentAttributeSet.addAttribute(CommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN, base64encode(idToken.getToken()));
        }
        xacmlBuilder.addEnvironmentAttributeSet(environmentAttributeSet);
    }


    private String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private AbacResultat resultatFraResponse(BiasedDecisionResponse response) {
        if (response.getBiasedDecision() == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }
        List<Advice> denyAdvice = response.getXacmlResponse().getAdvice();

        if (logger.isDebugEnabled()) {
            logger.debug("Deny fra PDP, advice var: " + LoggerUtils.toStringWithoutLineBreaks(denyAdvice)); //NOSONAR
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
                throw PdpFeil.FACTORY.indeterminateDecisionFeil(decision, response).toException();
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
            throw PdpFeil.FACTORY.ukjentObligationsFeil(obligations).toException();
        }
    }
}
