package no.nav.vedtak.sikkerhet.pdp;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.pdp.xacml.Advice;
import no.nav.vedtak.sikkerhet.pdp.xacml.Decision;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseMapper;

@Dependent
public class PdpKlientImpl implements PdpKlient {

    private static final Logger LOG = LoggerFactory.getLogger(PdpKlientImpl.class);

    private final PdpConsumer pdp;

    @Inject
    public PdpKlientImpl(PdpConsumer pdp) {
        this.pdp = pdp;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData) {
        var request = XacmlRequestMapper.lagXacmlRequest(beskyttetRessursAttributter, domene, appRessursData);
        var response = pdp.evaluate(request);
        var hovedresultat = resultatFraResponse(response);
        return new Tilgangsbeslutning(hovedresultat, beskyttetRessursAttributter, appRessursData);
    }

    private static AbacResultat resultatFraResponse(XacmlResponse response) {
        var decisions = XacmlResponseMapper.getDecisions(response);

        for (var decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw new TekniskException("F-080281",
                    String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", decision, response));
            }
        }

        var biasedDecision = createAggregatedDecision(decisions);
        handlObligation(response);

        if (biasedDecision == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }

        var denyAdvice = XacmlResponseMapper.getAdvice(response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: {}", LoggerUtils.toStringWithoutLineBreaks(denyAdvice));
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
        var ukjentadvice = denyAdvice.toString();
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        for (var decision : decisions) {
            if (decision != Decision.Permit)
                return Decision.Deny;
        }
        return Decision.Permit;
    }

    private static void handlObligation(XacmlResponse response) {
        var obligations = XacmlResponseMapper.getObligations(response);
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }
}
