package no.nav.vedtak.sikkerhet.pdp;

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
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.util.Tuple;

/**
 * Eksemple {@link XacmlRequestBuilderTjeneste} for enhetstest.
 */
@Dependent
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(XacmlRequestBuilderTjenesteImpl.class);

    public XacmlRequestBuilderTjenesteImpl() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(XACML10_ACTION_ACTION_ID,
                pdpRequest.getString(XACML10_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);
        List<Tuple<String, String>> identer = hentIdenter(pdpRequest, RESOURCE_FELLES_PERSON_FNR,
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

    private void populerSubjects(PdpRequest pdpRequest, XacmlRequestBuilder xacmlBuilder) {
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

    protected void populerResources(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest, Tuple<String, String> ident) {
        var attributter = byggRessursAttributter(pdpRequest);
        if (ident != null) {
            attributter.addAttribute(ident.getElement1(), ident.getElement2());
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

    private static List<Tuple<String, String>> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<Tuple<String, String>> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new Tuple<>(key, it)).toList());
        }
        return identer;
    }
}
