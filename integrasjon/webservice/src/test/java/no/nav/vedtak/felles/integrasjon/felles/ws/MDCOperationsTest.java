package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.log.mdc.MDCOperations;

class MDCOperationsTest {

    @Test
    void test_generateCallId() {

        String callId1 = MDCOperations.generateCallId();
        assertThat(callId1).isNotNull();
        String callId2 = MDCOperations.generateCallId();
        assertThat(callId2).isNotNull()
                .isNotEqualTo(callId1);
    }

    @Test
    void test_mdc() {
        MDCOperations.putToMDC("myKey", "myValue");
        assertThat(MDCOperations.getFromMDC("myKey")).isEqualTo("myValue");
        MDCOperations.remove("myKey");
        assertThat(MDCOperations.getFromMDC("myKey")).isNull();
    }
}
