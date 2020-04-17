package no.nav.vedtak.felles.integrasjon.sensu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SensuEventTest {

    @Test(expected = IllegalStateException.class)
    public void toSensuRequest_illegal_state_exception_metrikk_felter_kan_ikke_være_tomt() {
        SensuEvent.createSensuEvent("testMetric", Map.of()).toSensuRequest();
        fail("Forventer exception");
    }

    @Test
    public void toSensuRequest() {
        final SensuEvent data = SensuEvent.createSensuEvent("testMetric", Map.of("test", 1));
        final SensuEvent.SensuRequest sensuRequest = data.toSensuRequest();

        assertThat(sensuRequest).isNotNull();
        String jsonRequest = null;
        try {
            jsonRequest = sensuRequest.toJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertThat(jsonRequest).contains("test");
        assertThat(jsonRequest).contains("testMetric");
        assertThat(jsonRequest).contains("events_nano");
        assertThat(jsonRequest).contains("metric");
    }

    @Test
    public void toSensuRequest_default_tags_er_satt() {
        SensuEvent data = SensuEvent.createSensuEvent("test", Map.of("testMetric", 1));
        final SensuEvent.SensuRequest sensuRequest = data.toSensuRequest();

        String jsonRequest = null;
        try {
            jsonRequest = sensuRequest.toJson();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        assertThat(sensuRequest).isNotNull();
        assertThat(jsonRequest).isNotNull();
        assertThat(jsonRequest).contains("testMetric");
        assertThat(jsonRequest).contains("application");
        assertThat(jsonRequest).contains("cluster");
        assertThat(jsonRequest).contains("namespace");
    }
}
