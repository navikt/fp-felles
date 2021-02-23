package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.finn.unleash.strategy.Strategy;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;

class ByEnvironmentStrategyTest {

    @BeforeEach
    void setUp() {
        System.setProperty(EnvironmentProperty.NAIS_NAMESPACE, "t10");
    }

    @Test
    void testStrategyName() {
        Strategy strategy = new ByEnvironmentStrategy();
        assertThat(strategy.getName()).isEqualTo("byEnvironment");
    }

    @Test
    void testNullParameterIsDisabled() {
        Strategy strategy = new ByEnvironmentStrategy();
        assertThat(strategy.isEnabled(null)).isFalse();
    }

    @Test
    void testEmptyParameterIsDisabled() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.emptyMap();
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    void isEnabledParametersContainingNullValue() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, null);
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    void isEnabledParametersContainingWrongEnvironment() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "p");
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    void isEnabledParametersContainingRightEnvironment() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "t10");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

    @Test
    void isEnabledParametersContainingMultipleEnvironments() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "t10,local,callo");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

    @Test
    void isEnabledParametersContainingMultipleEnvironmentsWithSpaces() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, " najis , , q10 , t10");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

}
