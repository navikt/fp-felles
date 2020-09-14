package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

public class AbacDtoTest {

    @Test
    public void skal_ikke_ha_noen_metode_som_begynner_med_get_da_det_blir_med_i_autogenerert_sysdok() throws Exception {
        for (Method method : AbacDto.class.getDeclaredMethods()) {
            assertThat(method.getName()).doesNotStartWith("get");
        }

    }

}
