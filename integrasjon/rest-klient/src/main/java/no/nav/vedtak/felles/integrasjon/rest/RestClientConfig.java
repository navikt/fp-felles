package no.nav.vedtak.felles.integrasjon.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Documented
public @interface RestClientConfig {

    /**
     * Type of token and flow required by the endpoint
     */
    @Nonbinding TokenFlow tokenConfig() default TokenFlow.ADAPTIVE;

    /**
     * Application running in the namespace of Team Foreldrepenger. Implies endpoint (and later on scopes)
     */
    @Nonbinding FpApplication application() default FpApplication.NONFP;

    /**
     * Name of property holding the URI for the endpoint for cluster/namespace
     */
    @Nonbinding String endpointProperty() default "";

    /**
     * Default value for endpoint if endpoint property is not set
     */
    @Nonbinding String endpointDefault() default "";

    /**
     * Name of property holding the scopes for the endpoint
     */
    @Nonbinding String scopesProperty() default "";

    /**
     * Default value for scopes if scopes property is not set
     */
    @Nonbinding String scopesDefault() default "openid";
}
