package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class ArbeidsfordelingRestTest {

    private static final ObjectWriter WRITER = DefaultJsonMapper.getObjectMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().reader();

    @Test
    void test_request() throws Exception {
        var request = ArbeidsfordelingRequest.ny()
                .medTema("FOR")
                .medOppgavetype("BEH_SAK_VL")
                .medBehandlingstype("ae0028")
                .build();

        String json = DefaultJsonMapper.toJson(request);
        ArbeidsfordelingRequest roundTripped = DefaultJsonMapper.fromJson(json, ArbeidsfordelingRequest.class);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getTema()).isEqualTo("FOR");
        assertThat(roundTripped.getBehandlingstype()).isEqualTo("ae0028");
    }

    @Test
    void test_response() throws Exception {
        var respons = new ArbeidsfordelingResponse("4806", "Drammen", "Aktiv", "FPY");
        String json = WRITER.writeValueAsString(respons);
        ArbeidsfordelingResponse roundTripped = DefaultJsonMapper.fromJson(json, ArbeidsfordelingResponse.class);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.enhetNr()).isEqualTo("4806");
    }
}
