package no.nav.foreldrepenger.sikkerhet.abac.pdp;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacAttributtNøkkel;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdSubject;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlAttributeSet;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequestBuilder;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.util.env.Environment;

public class XacmlRequestMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PdpImpl.class);
    private static final Environment ENV = Environment.current();
    private static final String DEFAULT_DOMENE_FORELDREPENGER = "foreldrepenger";

    public static XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        var identer = hentIdenter(pdpRequest);

        if (identer.isEmpty()) {
            populerResourcesSet(xacmlBuilder, pdpRequest, null);
        } else {
            identer.forEach(ident -> populerResourcesSet(xacmlBuilder, pdpRequest, ident));
        }

        populerActionSet(xacmlBuilder, pdpRequest.getActionId());
        populerEnvironmentSet(xacmlBuilder, pdpRequest);
        pdpRequest.getIdSubject().ifPresent(subject -> populerSubjectSet(xacmlBuilder, subject));
        return xacmlBuilder;
    }

    private static XacmlRequest.Pair getPepIdInfo(final PdpRequest pdpRequest) {
        return new XacmlRequest.Pair(AbacAttributtNøkkel.ENVIRONMENT_PEP_ID, pdpRequest.getPepId().orElse(getPepId()));
    }

    private static XacmlRequest.Pair getTokenInfo(IdToken idToken) {
        switch (idToken.getTokenType()) {
            case OIDC:
            case TOKENX: {
                return getOidcTokenInfo(idToken);
            }
            case SAML: {
                LOG.trace("Legger på token med type saml");
                return new XacmlRequest.Pair(AbacAttributtNøkkel.ENVIRONMENT_SAML_TOKEN, base64encode(idToken.getToken()));
            }
        }
        throw new IllegalArgumentException("En gyldig token må være satt.");
    }

    private static XacmlRequest.Pair getOidcTokenInfo(final IdToken idToken) {
        String key;
        var tokenType = idToken.getTokenType();
        if (tokenType.equals(TokenType.OIDC)) {
            key = AbacAttributtNøkkel.ENVIRONMENT_OIDC_TOKEN_BODY;
        } else if (tokenType.equals(TokenType.TOKENX)) {
            key = AbacAttributtNøkkel.ENVIRONMENT_TOKENX_TOKEN_BODY;
        } else {
            throw new IllegalArgumentException(String.format("Ukjent token type: %s, forventer OIDC eller TOKENX", tokenType));
        }
        LOG.trace("Legger ved {} token på {}", tokenType, key);
        try {
            return new XacmlRequest.Pair(key, SignedJWT.parse(idToken.getToken()).getPayload().toBase64URL().toString());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ukjent token type");
        }
    }

    private static List<XacmlRequest.Pair> getSubjectInfo(IdSubject idSubject) {
        List<XacmlRequest.Pair> subjectPairs = new ArrayList<>();
        subjectPairs.add(new XacmlRequest.Pair(AbacAttributtNøkkel.SUBJECT_ID, idSubject.getSubjectId()));
        subjectPairs.add(new XacmlRequest.Pair(AbacAttributtNøkkel.SUBJECT_TYPE, idSubject.getSubjectType()));
        idSubject.getSubjectLevel().ifPresent(level -> subjectPairs.add(new XacmlRequest.Pair(AbacAttributtNøkkel.SUBJECT_LEVEL, level)));
        return subjectPairs;
    }

    static void populerSubjectSet(final XacmlRequestBuilder builder, final IdSubject subject) {
        var actionAttributes = new XacmlAttributeSet();
        var subjectInfo = getSubjectInfo(subject);
        subjectInfo.forEach(pair -> actionAttributes.addAttribute(pair.getAttributeId(), pair.getValue()));
        builder.addSubjectAttributeSet(actionAttributes);
    }

    static void populerActionSet(XacmlRequestBuilder builder, ActionType actionType) {
        var actionAttributes = new XacmlAttributeSet();
        actionAttributes.addAttribute(AbacAttributtNøkkel.ACTION_ACTION_ID, actionType.getEksternKode());
        builder.addActionAttributeSet(actionAttributes);
    }

    static void populerEnvironmentSet(XacmlRequestBuilder builder, PdpRequest pdpRequest) {
        var environmentAttributes = new XacmlAttributeSet();
        var pepInfo = getPepIdInfo(pdpRequest);
        environmentAttributes.addAttribute(pepInfo.getAttributeId(), pepInfo.getValue());
        var tokenType = getTokenInfo(pdpRequest.getIdToken());
        environmentAttributes.addAttribute(tokenType.getAttributeId(), tokenType.getValue());
        builder.addEnvironmentAttributeSet(environmentAttributes);
    }

    private static String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private static String getPepId() { return ENV.appName(); }

    private static void populerResourcesSet(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest, Ident ident) {
        Set<String> aksjonspunktTyper = pdpRequest.getAksjonspunkter();
        if (aksjonspunktTyper.isEmpty()) {
            xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, null));
        } else {
            for (String aksjonspunktType : aksjonspunktTyper) {
                xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident, aksjonspunktType));
            }
        }
    }

    protected static XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, Ident ident, String aksjonsounktType) {
        var resourceAttributeSet = new XacmlAttributeSet();

        resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_DOMENE, pdpRequest.getDomene().orElse(DEFAULT_DOMENE_FORELDREPENGER));
        resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_RESOURCE_TYPE, pdpRequest.getResourceType());

        pdpRequest.getFagsakStatus().ifPresent(s -> resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, s.getEksternKode()));
        pdpRequest.getBehandlingStatus().ifPresent(s -> resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, s.getEksternKode()));
        pdpRequest.getAnsvarligSaksbenandler().ifPresent(s -> resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, s));

        pdpRequest.getAnnenPartAktørId().ifPresent(s -> resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ANNEN_PART, s));
        pdpRequest.getAleneomsorg().ifPresent(s -> resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ALENEOMSORG, s.toString()));

        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.key, ident.ident);
        }

        if (aksjonsounktType != null) {
            resourceAttributeSet.addAttribute(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, aksjonsounktType);
        }

        return resourceAttributeSet;
    }


    private static List<Ident> hentIdenter(PdpRequest pdpRequest) {
        List<Ident> identer = pdpRequest.getAktørIder()
            .stream()
            .map(it -> new Ident(AbacAttributtNøkkel.RESOURCE_PERSON_AKTOERID, it))
            .collect(Collectors.toList());

        identer.addAll(pdpRequest.getPersonnummere()
            .stream()
            .map(it -> new Ident(AbacAttributtNøkkel.RESOURCE_PERSON_FNR, it))
            .collect(Collectors.toList()));

        return identer;
    }

    public static class Ident {
        private String key;
        private String ident;

        public Ident(String key, String ident) {
            this.key = key;
            this.ident = ident;
        }

        public String Key() {
            return key;
        }

        public String Ident() {
            return ident;
        }
    }
}
