package no.nav.vedtak.felles.integrasjon.unleash;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class EnvironmentPropertyTest {
    @Test
    public void skal_utlede_rett_miljø() {
        System.setProperty(EnvironmentProperty.NAIS_NAMESPACE, "default");
        var environment = EnvironmentProperty.getEnvironmentName();

        System.setProperty(EnvironmentProperty.NAIS_CLUSTER_NAME, "prod-fss");
        environment = EnvironmentProperty.getEnvironmentName();
        assertThat(environment).hasValue(EnvironmentProperty.PROD);
    }

    @Test
    public void skal_utlede_q_for_preprod() {
        System.setProperty(EnvironmentProperty.NAIS_NAMESPACE, "default");
        var environment = EnvironmentProperty.getEnvironmentName();

        System.setProperty(EnvironmentProperty.NAIS_CLUSTER_NAME, "dev-fss");
        environment = EnvironmentProperty.getEnvironmentName();
        assertThat(environment).hasValue(EnvironmentProperty.PREPROD);
    }
}
