package no.nav.vedtak.sikkerhet.pdp2;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.SUBJECT_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_SUBJECT_ID;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlRequestBuilder2;

/**
 * Eksemple {@link XacmlRequestBuilder2Tjeneste} for enhetstest.
 */
@Dependent
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilder2Tjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(XacmlRequestBuilderTjenesteImpl.class);

    public XacmlRequestBuilderTjenesteImpl() {
    }

    @Override
    public XacmlRequestBuilder2 lagXacmlRequestBuilder2(PdpRequest pdpRequest) {
        XacmlRequestBuilder2 xacmlBuilder = new XacmlRequestBuilder2();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(XACML10_ACTION_ACTION_ID,
                pdpRequest.getString(XACML10_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);
        var identer = hentIdenter(pdpRequest, RESOURCE_FELLES_PERSON_FNR,
                RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);

        if (identer.isEmpty()) {
            populerResources(xacmlBuilder, pdpRequest, null);
        } else {
            for (var ident : identer) {
                populerResources(xacmlBuilder, pdpRequest, ident);
            }
        }

        populerSubjects(pdpRequest, xacmlBuilder);

        return xacmlBuilder;
    }

    private void populerSubjects(PdpRequest pdpRequest, XacmlRequestBuilder2 xacmlBuilder) {
        var attrs = new XacmlAttributeSet();
        var found = false;

        if (pdpRequest.get(XACML10_SUBJECT_ID) != null) {
            attrs.addAttribute(XACML10_SUBJECT_ID, pdpRequest.getString(XACML10_SUBJECT_ID));
            found = true;
        }
        if (pdpRequest.get(SUBJECT_TYPE) != null) {
            attrs.addAttribute(SUBJECT_TYPE, pdpRequest.getString(SUBJECT_TYPE));
            found = true;
        }
        if (found) {
            LOG.trace("Legger til subject attributter {}", attrs);
            xacmlBuilder.addSubjectAttributeSet(attrs);
        }
        LOG.trace("Legger IKKE til suject attributter");
    }

    protected void populerResources(XacmlRequestBuilder2 xacmlBuilder, PdpRequest pdpRequest, Ident ident) {
        var attributter = byggRessursAttributter(pdpRequest);
        if (ident != null) {
            attributter.addAttribute(ident.one(), ident.two());
        }
        xacmlBuilder.addResourceAttributeSet(attributter);
    }

    protected XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest) {
        var resourceAttributeSet = new XacmlAttributeSet();

        resourceAttributeSet.addAttribute(RESOURCE_FELLES_DOMENE,
                pdpRequest.getString(RESOURCE_FELLES_DOMENE));

        resourceAttributeSet.addAttribute(RESOURCE_FELLES_RESOURCE_TYPE,
                pdpRequest.getString(RESOURCE_FELLES_RESOURCE_TYPE));

        return resourceAttributeSet;
    }

    protected void setOptionalValueinAttributeSet(XacmlAttributeSet resourceAttributeSet, PdpRequest pdpRequest, String key) {
        pdpRequest.getOptional(key).ifPresent(s -> resourceAttributeSet.addAttribute(key, s));
    }

    private static List<Ident> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<Ident> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new Ident(key, it)).toList());
        }
        return identer;
    }

    private record Ident(String one, String two) {

    }
}
