package no.nav.vedtak.openapi.converters;

import java.lang.annotation.Annotation;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;

public class NoJsonSubTypesAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    protected <A extends Annotation> A _findAnnotation(Annotated ann, Class<A> annoClass) {
        if (JsonSubTypes.class.equals(annoClass)) {
            return  null;
        }
        return super._findAnnotation(ann, annoClass);
    }
}
