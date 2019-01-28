package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import no.finn.unleash.strategy.Strategy;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;

public class ByEnvironmentStrategyTest {

    @Before
    public void setUp() {
      System.setProperty(EnvironmentProperty.APP_ENVIRONMENT_NAME, "t10");
    }

    @Test
    public void testStrategyName() {
        Strategy strategy = new ByEnvironmentStrategy();
        assertThat(strategy.getName()).isEqualTo("byEnvironment");
    }

    @Test
    public void testNullParameterIsDisabled() {
        Strategy strategy = new ByEnvironmentStrategy();
        assertThat(strategy.isEnabled(null)).isFalse();
    }

    @Test
    public void testEmptyParameterIsDisabled() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.emptyMap();
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    public void isEnabledParametersContainingNullValue() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, null);
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    public void isEnabledParametersContainingWrongEnvironment() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "p");
        assertThat(strategy.isEnabled(parameters)).isFalse();
    }

    @Test
    public void isEnabledParametersContainingRightEnvironment() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "t10");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

    @Test
    public void isEnabledParametersContainingMultipleEnvironments() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, "t10,local,callo");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

    @Test
    public void isEnabledParametersContainingMultipleEnvironmentsWithSpaces() {
        Strategy strategy = new ByEnvironmentStrategy();
        Map<String, String> parameters = Collections.singletonMap(ByEnvironmentStrategy.ENV_KEY, " najis , , q10 , t10");
        assertThat(strategy.isEnabled(parameters)).isTrue();
    }

}