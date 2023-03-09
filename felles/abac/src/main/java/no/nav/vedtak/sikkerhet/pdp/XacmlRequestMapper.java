package no.nav.vedtak.sikkerhet.pdp;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.pdp.xacml.Category;
import no.nav.vedtak.sikkerhet.pdp.xacml.NavFellesAttributter;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequest;

import java.util.*;

public class XacmlRequestMapper {

    private static final Environment ENV = Environment.current();

    public static XacmlRequest lagXacmlRequest(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData) {
        var actionAttributes = new XacmlRequest.Attributes(List.of(actionInfo(beskyttetRessursAttributter)));

        List<XacmlRequest.AttributeAssignment> envList = new ArrayList<>();
        envList.add(getPepIdInfo(beskyttetRessursAttributter));
        envList.addAll(getTokenEnvironmentAttrs(beskyttetRessursAttributter));

        var envAttributes = new XacmlRequest.Attributes(envList);

        List<XacmlRequest.Attributes> resourceAttributes = new ArrayList<>();
        var identer = hentIdenter(appRessursData);
        if (identer.isEmpty()) {
            resourceAttributes.add(resourceInfo(beskyttetRessursAttributter, domene, appRessursData, null));
        } else {
            identer.forEach(ident -> resourceAttributes.add(resourceInfo(beskyttetRessursAttributter, domene, appRessursData, ident)));
        }

        Map<Category, List<XacmlRequest.Attributes>> requestMap = new HashMap<>();
        requestMap.put(Category.Action, List.of(actionAttributes));
        requestMap.put(Category.Environment, List.of(envAttributes));
        requestMap.put(Category.Resource, resourceAttributes);
        return new XacmlRequest(requestMap);
    }

    private static XacmlRequest.Attributes resourceInfo(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData, Ident ident) {
        List<XacmlRequest.AttributeAssignment> attributes = new ArrayList<>();

        attributes.add(new XacmlRequest.AttributeAssignment(NavFellesAttributter.RESOURCE_FELLES_DOMENE, domene));
        attributes.add(new XacmlRequest.AttributeAssignment(NavFellesAttributter.RESOURCE_FELLES_RESOURCE_TYPE, beskyttetRessursAttributter.getResourceType()));

        appRessursData.getResources().values().stream()
            .map(ressursData -> new XacmlRequest.AttributeAssignment(ressursData.nøkkel().getKey(), ressursData.verdi()))
            .forEach(attributes::add);

        if (ident != null) {
            attributes.add(new XacmlRequest.AttributeAssignment(ident.key(), ident.ident()));
        }
        return new XacmlRequest.Attributes(attributes);
    }

    private static XacmlRequest.AttributeAssignment actionInfo(final BeskyttetRessursAttributter beskyttetRessursAttributter) {
        return new XacmlRequest.AttributeAssignment(NavFellesAttributter.XACML10_ACTION_ID, beskyttetRessursAttributter.getActionType().getEksternKode());
    }

    private static XacmlRequest.AttributeAssignment getPepIdInfo(final BeskyttetRessursAttributter beskyttetRessursAttributter) {
        return new XacmlRequest.AttributeAssignment(NavFellesAttributter.ENVIRONMENT_FELLES_PEP_ID,
            Optional.ofNullable(beskyttetRessursAttributter.getPepId()).orElse(getPepId()));
    }

    private static List<XacmlRequest.AttributeAssignment> getTokenEnvironmentAttrs(final BeskyttetRessursAttributter beskyttetRessursAttributter) {
        String envTokenBodyAttributt = switch (beskyttetRessursAttributter.getToken().getTokenType()) {
            case OIDC -> NavFellesAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
            case TOKENX -> NavFellesAttributter.ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
            case SAML -> NavFellesAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
        };
        var assignement = new XacmlRequest.AttributeAssignment(envTokenBodyAttributt, beskyttetRessursAttributter.getToken().getTokenBody());
        return List.of(assignement);
    }

    private static String getPepId() {
        return ENV.getNaisAppName();
    }

    private static List<Ident> hentIdenter(AppRessursData appRessursData) {
        List<Ident> identer = new ArrayList<>();
        appRessursData.getAktørIdSet().stream()
            .map(it -> new Ident(NavFellesAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, it))
            .forEach(identer::add);

        appRessursData.getFødselsnumre().stream()
            .map(it -> new Ident(NavFellesAttributter.RESOURCE_FELLES_PERSON_FNR, it))
            .forEach(identer::add);

        return identer;
    }

    public static record Ident(String key, String ident) {
    }
}
