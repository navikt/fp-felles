package no.nav.foreldrepenger.sikkerhet.abac.pdp2;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacAttributtNøkkel;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdSubject;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.PdpImpl;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.util.env.Environment;

public class NyXacmlRequestMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PdpImpl.class);
    private static final Environment ENV = Environment.current();
    private static final String DEFAULT_DOMENE_FORELDREPENGER = "foreldrepenger";

    public static XacmlRequest lagXacmlRequest(PdpRequest pdpRequest) {
        var actionAttributes = new XacmlRequest.AttributeSet(
            List.of(getActionInfo(pdpRequest))
        );
        var envAttributes = new XacmlRequest.AttributeSet(
            List.of(
                getPepIdInfo(pdpRequest),
                getTokenInfo(pdpRequest.getIdToken())
            )
        );
        var subjectAttributes = new XacmlRequest.AttributeSet(
            pdpRequest.getIdSubject().map(NyXacmlRequestMapper::getSubjectInfo).orElse(List.of())
        );
        var resourceAttributes = getResourceInfo(pdpRequest);
        var request = new XacmlRequest.Request(actionAttributes, envAttributes, resourceAttributes, subjectAttributes.Attributt().isEmpty() ? null : subjectAttributes);
        return new XacmlRequest(request);
    }

    private static List<XacmlRequest.AttributeSet> getResourceInfo(final PdpRequest pdpRequest) {
        var resourceAttributes = new ArrayList<XacmlRequest.AttributeSet>();
        var identer = hentIdenter(pdpRequest);
        if (identer.isEmpty()) {
            resourceAttributes.addAll(getResourceInfo(pdpRequest, null));
        } else {
            identer.forEach(ident -> resourceAttributes.addAll(getResourceInfo(pdpRequest, ident)));
        }
        return resourceAttributes;
    }

    private static List<XacmlRequest.AttributeSet> getResourceInfo(final PdpRequest pdpRequest, Ident ident) {
        var resourceAttributes = new ArrayList<XacmlRequest.AttributeSet>();
        var aksjonspunktTyper = pdpRequest.getAksjonspunkter();
        if (aksjonspunktTyper.isEmpty()) {
            resourceAttributes.add(getResourceInfo(pdpRequest, ident, null));
        } else {
            aksjonspunktTyper.forEach(ap -> resourceAttributes.add(getResourceInfo(pdpRequest, ident, ap)));
        }
        return resourceAttributes;
    }

    private static XacmlRequest.AttributeSet getResourceInfo(PdpRequest pdpRequest, Ident ident, String aksjonsounktType) {
        var attributes = new ArrayList<XacmlRequest.Pair>();

        attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_DOMENE, pdpRequest.getDomene().orElse(DEFAULT_DOMENE_FORELDREPENGER)));
        attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_RESOURCE_TYPE, pdpRequest.getResourceType()));

        pdpRequest.getFagsakStatus().ifPresent(s -> attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, s.getEksternKode())));
        pdpRequest.getBehandlingStatus().ifPresent(s -> attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, s.getEksternKode())));
        pdpRequest.getAnsvarligSaksbenandler().ifPresent(s -> attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER, s)));

        pdpRequest.getAnnenPartAktørId().ifPresent(s -> attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ANNEN_PART, s)));
        pdpRequest.getAleneomsorg().ifPresent(s -> attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_ALENEOMSORG, s.toString())));

        if (ident != null) {
            attributes.add(new XacmlRequest.Pair(ident.key, ident.ident));
        }

        if (aksjonsounktType != null) {
            attributes.add(new XacmlRequest.Pair(AbacAttributtNøkkel.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, aksjonsounktType));
        }

        return new XacmlRequest.AttributeSet(attributes);
    }

    private static XacmlRequest.Pair getActionInfo(final PdpRequest pdpRequest) {
        return new XacmlRequest.Pair(AbacAttributtNøkkel.ACTION_ACTION_ID, pdpRequest.getActionId().getEksternKode());
    }

    private static XacmlRequest.Pair getPepIdInfo(final PdpRequest pdpRequest) {
        return new XacmlRequest.Pair(AbacAttributtNøkkel.ENVIRONMENT_PEP_ID, pdpRequest.getPepId().orElse(getPepId()));
    }

    private static XacmlRequest.Pair getTokenInfo(IdToken idToken) {
        switch (idToken.getTokenType()) {
            case OIDC, TOKENX -> {
                return getOidcTokenInfo(idToken);
            }
            case SAML -> {
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
    private static String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private static String getPepId() { return ENV.appName(); }


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

    private record Ident(String key, String ident) {}
}
