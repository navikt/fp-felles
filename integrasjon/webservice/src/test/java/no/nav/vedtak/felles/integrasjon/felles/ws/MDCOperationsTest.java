package no.nav.vedtak.felles.integrasjon.felles.ws;

import no.nav.vedtak.log.mdc.MDCOperations;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MDCOperationsTest {

    public static final String DUMMY_FNR = "00000000000";

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

    @Test
    void mask_fnr() {
        MDCOperations.putUserId(DUMMY_FNR);
        var userId = MDCOperations.getUserId();
        assertThat(userId).isEqualTo("000000*****");

        MDCOperations.putUserId("ikkefnr");
        var userId2 = MDCOperations.getUserId();
        assertThat(userId2).isEqualTo("ikkefnr");
    }
}
