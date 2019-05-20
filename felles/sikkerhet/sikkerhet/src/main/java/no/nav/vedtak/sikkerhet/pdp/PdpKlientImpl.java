package no.nav.vedtak.sikkerhet.pdp;

import static no.nav.abac.arkiv.xacml.ArkivAttributter.RESOURCE_ARKIV_GSAK_SAKSID;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.FORELDREPENGER_OPPGAVESTYRING_AVDELINGSENHET;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_ALENEOMSORG;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_ANNEN_PART;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS;
import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abac.common.xacml.CommonAttributter;
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

@Dependent
public class PdpKlientImpl implements PdpKlient {

    private static final Logger logger = LoggerFactory.getLogger(PdpKlientImpl.class);
    private static final String ABAC_DOMENE = "foreldrepenger";
    private static final String SP_BEREGNING_ABAC_DOMENE = "sykepenger"; //TODO HUMLE midlertidig til felles splittes i FP og SP

    private PdpConsumer pdpConsumer;
    private String pepId;

    @Inject
    public PdpKlientImpl(PdpConsumer pdpConsumer, @KonfigVerdi("systembruker.username") String pepId) {
        this.pdpConsumer = pdpConsumer;
        this.pepId = pepId;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = lagXamlRequestBuilder(pdpRequest);
        XacmlResponseWrapper response = pdpConsumer.evaluate(xacmlBuilder);
        BiasedDecisionResponse biasedResponse = evaluateWithBias(response);
        AbacResultat hovedresultat = resultatFraResponse(biasedResponse);
        return new Tilgangsbeslutning(hovedresultat, response.getDecisions(), pdpRequest);
    }

    XacmlRequestBuilder lagXamlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getXacmlAction());
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        int antall = pdpRequest.antallResources();
        for (int i = 0; i < antall; i++) {
            XacmlAttributeSet resourceAttributeSet = byggXacmlResourceAttrSet(pdpRequest, i);
            xacmlBuilder.addResourceAttributeSet(resourceAttributeSet);
        }

        XacmlAttributeSet environmentAttributeSet = new XacmlAttributeSet();
        environmentAttributeSet.addAttribute(ENVIRONMENT_FELLES_PEP_ID, pepId);

        AbacIdToken idToken = pdpRequest.getToken();
        if (idToken.erOidcToken()) {
            environmentAttributeSet.addAttribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, JwtUtil.getJwtBody(idToken.getToken()));
        } else {
            environmentAttributeSet.addAttribute(ENVIRONMENT_FELLES_SAML_TOKEN, base64encode(idToken.getToken()));
        }
        xacmlBuilder.addEnvironmentAttributeSet(environmentAttributeSet);
        return xacmlBuilder;
    }

    private String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private XacmlAttributeSet byggXacmlResourceAttrSet(PdpRequest pdpRequest, int index) {

        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        if ("srvspberegning".equals(pepId)) { //TODO HUMLE midlertidig til felles splittes i FP og SP
            resourceAttributeSet.addAttribute(RESOURCE_FELLES_DOMENE, SP_BEREGNING_ABAC_DOMENE);
        } else {
            resourceAttributeSet.addAttribute(RESOURCE_FELLES_DOMENE, ABAC_DOMENE);
        }
        resourceAttributeSet.addAttribute(RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getXacmlResourceType());

        int antallFnrPåRequest = pdpRequest.getAntallFnr();
        if (index < antallFnrPåRequest) {
            Optional<String> fnr = pdpRequest.getFnrForIndex(index);
            fnr.ifPresent(a -> resourceAttributeSet.addAttribute(RESOURCE_FELLES_PERSON_FNR, a));
        } else {
            Optional<String> aktørId = pdpRequest.getAktørIdForIndex(index-antallFnrPåRequest);
            aktørId.ifPresent(a -> resourceAttributeSet.addAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, a));
        }
        Optional<String> aksjonspunktType = pdpRequest.getAksjonspunktTypeForIndex(index);
        aksjonspunktType.ifPresent(a -> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, a));
        pdpRequest.getXacmlSakstatus().ifPresent(s -> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, s));
        pdpRequest.getXacmlBehandlingStatus().ifPresent(b -> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, b));
        pdpRequest.getAnsvarligSaksbehandler().ifPresent(a -> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, a));
        pdpRequest.getSaksummerForIndex(index).ifPresent(s -> resourceAttributeSet.addAttribute(RESOURCE_ARKIV_GSAK_SAKSID, s));
        pdpRequest.getOppgavestyringEnhetForIndex(index).ifPresent(a -> resourceAttributeSet.addAttribute(FORELDREPENGER_OPPGAVESTYRING_AVDELINGSENHET, a));
        pdpRequest.getOpgittAleneomsorg().ifPresent(a-> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_ALENEOMSORG, a.toString()));
        pdpRequest.getAnnenPartAktørId().ifPresent(a-> resourceAttributeSet.addAttribute(RESOURCE_FORELDREPENGER_ANNEN_PART, a));
        
        return resourceAttributeSet;
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
