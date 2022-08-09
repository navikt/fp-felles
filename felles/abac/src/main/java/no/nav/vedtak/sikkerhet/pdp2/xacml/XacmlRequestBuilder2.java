package no.nav.vedtak.sikkerhet.pdp2.xacml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class XacmlRequestBuilder2 {

    private Map<Category, List<XacmlAttributeSet>> attributeSets = new EnumMap<>(Category.class);

    public XacmlRequestBuilder2 addResourceAttributeSet(XacmlAttributeSet attributeSet) {
        addAttributeSetInCategory(Category.Resource, attributeSet);
        return this;
    }

    public XacmlRequestBuilder2 addEnvironmentAttributeSet(XacmlAttributeSet attributeSet) {
        addAttributeSetInCategory(Category.Environment, attributeSet);
        return this;
    }

    public XacmlRequestBuilder2 addActionAttributeSet(XacmlAttributeSet attributeSet) {
        addAttributeSetInCategory(Category.Action, attributeSet);
        return this;
    }

    public XacmlRequestBuilder2 addSubjectAttributeSet(XacmlAttributeSet attributeSet) {
        addAttributeSetInCategory(Category.AccessSubject, attributeSet);
        return this;
    }

    private void addAttributeSetInCategory(Category category, XacmlAttributeSet decisionPoint) {

        if (attributeSets.containsKey(category)) {
            attributeSets.get(category).add(decisionPoint);
        } else {
            List<XacmlAttributeSet> setList = new ArrayList<>();
            setList.add(decisionPoint);
            attributeSets.put(category, setList);
        }
    }

    public XacmlRequest build() {
        var attributeMap = new LinkedHashMap<Category, List<XacmlRequest.Attributes>>();

        Set<Category> keys = attributeSets.keySet();
        for (Category xacmlCategory : keys) {
            attributeMap.putIfAbsent(xacmlCategory, new ArrayList<>());
            List<XacmlAttributeSet> attrsList = attributeSets.get(xacmlCategory);
            var alist = attrsList.stream()
                .map(XacmlAttributeSet::getAttributes)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            var rq = new XacmlRequest.Attributes(alist);
            attributeMap.get(xacmlCategory).add(rq);
        }

        var request = new XacmlRequest(attributeMap);

        attributeSets.clear();
        return request;
    }
}
