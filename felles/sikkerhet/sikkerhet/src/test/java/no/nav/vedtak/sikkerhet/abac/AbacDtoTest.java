package no.nav.vedtak.sikkerhet.abac;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class AbacDtoTest {

    @Test
    public void skal_ikke_ha_noen_metode_som_begynner_med_get_da_det_blir_med_i_autogenerert_sysdok() throws Exception {
        for (Method method : AbacDto.class.getDeclaredMethods()) {
            assertThat(method.getName()).doesNotStartWith("get");
        }

    }

}
