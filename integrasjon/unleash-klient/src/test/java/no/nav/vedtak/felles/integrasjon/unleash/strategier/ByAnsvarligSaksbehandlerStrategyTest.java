package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.finn.unleash.UnleashContext;
import no.finn.unleash.strategy.Strategy;
import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;

public class ByAnsvarligSaksbehandlerStrategyTest {

    private UnleashContext unleashContext;
    private Strategy strategy;

    @BeforeEach
    public void setUp() {
        System.setProperty(EnvironmentProperty.NAIS_NAMESPACE, "t10");
        unleashContext = UnleashContext.builder()
                .addProperty(ByAnsvarligSaksbehandlerStrategy.SAKSBEHANDLER_IDENT, "10001")
                .build();
        strategy = new ByAnsvarligSaksbehandlerStrategy();
    }

    @Test
    public void getName() {
        assertThat(strategy.getName()).isEqualTo("byAnsvarligSaksbehandler");
    }

    @Test
    public void isEnabledUtenUnleashContext() {
        Map<String, String> saksBehandlerMap = new HashMap<>();
        assertThat(strategy.isEnabled(saksBehandlerMap)).isFalse();
    }

    @Test
    public void testIsEnabledUtenRiktigMiljø() {
        Map<String, String> saksbehandlerMap = new HashMap<>();
        saksbehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_SAKSBEHANDLER, "10001,10002,10003");
        saksbehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_MILJØ, "q10");
        assertThat(strategy.isEnabled(saksbehandlerMap, unleashContext)).isFalse();
    }

    @Test
    public void testIsEnabledMedUnleashContextOgLikCurrentSaksbehandler() {
        Map<String, String> saksBehandlerMap = new HashMap<>();
        saksBehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_SAKSBEHANDLER, "10001,10002,10003");
        saksBehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_MILJØ, "t10,q10");
        assertThat(strategy.isEnabled(saksBehandlerMap, unleashContext)).isTrue();
    }

    @Test
    public void testIsEnabledMedUnleashContextOgIkkeLikCurrentSaksbehandler() {
        Map<String, String> saksBehandlerMap = new HashMap<>();
        saksBehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_SAKSBEHANDLER, "10002,10003");
        assertThat(strategy.isEnabled(saksBehandlerMap, unleashContext)).isFalse();
    }

    @Test
    public void testIsEnabledMedUnleashContextOgTomSaksbehandler() {
        Map<String, String> saksBehandlerMap = new HashMap<>();
        assertThat(strategy.isEnabled(saksBehandlerMap, unleashContext)).isFalse();
    }

    @Test
    public void testIsEnabledMedUnleashContextOgNullSaksbehandler() {
        unleashContext = UnleashContext.builder()
                .build();
        assertThat(strategy.isEnabled(null, unleashContext)).isFalse();
    }

    @Test
    public void testIsEnabledMedUnleashContextOgIkkeSaksbehandler() {
        Map<String, String> saksBehandlerMap = new HashMap<>();
        saksBehandlerMap.put(ByAnsvarligSaksbehandlerStrategy.UNLEASH_PROPERTY_NAME_SAKSBEHANDLER, null);
        assertThat(strategy.isEnabled(saksBehandlerMap, unleashContext)).isFalse();
    }
}
