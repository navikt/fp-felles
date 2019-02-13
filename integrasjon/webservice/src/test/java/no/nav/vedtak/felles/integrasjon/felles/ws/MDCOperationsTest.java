package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.vedtak.log.mdc.MDCOperations;

public class MDCOperationsTest {

    @Test
    public void test_generateCallId() {

        String callId1 = MDCOperations.generateCallId();

        assertThat(callId1).isNotNull();

        String callId2 = MDCOperations.generateCallId();

        assertThat(callId2).isNotNull();
        assertThat(callId2).isNotEqualTo(callId1);
    }

    @Test
    public void test_mdc() {
        MDCOperations.putToMDC("myKey", "myValue");

        assertThat(MDCOperations.getFromMDC("myKey")).isEqualTo("myValue");

        MDCOperations.remove("myKey");

        assertThat(MDCOperations.getFromMDC("myKey")).isNull();
    }
}
