package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

class ArbeidsfordelingRestTest {

    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = MAPPER.reader();

    @Test
    void test_request() throws Exception {
        var request = ArbeidsfordelingRequest.ny()
                .medTema("FOR")
                .medOppgavetype("BEH_SAK_VL")
                .medBehandlingstype("ae0028")
                .build();

        String json = WRITER.writeValueAsString(request);
        ArbeidsfordelingRequest roundTripped = READER.forType(ArbeidsfordelingRequest.class).readValue(json);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getTema()).isEqualTo("FOR");
        assertThat(roundTripped.getBehandlingstype()).isEqualTo("ae0028");
    }

    @Test
    void test_response() throws Exception {
        var respons = new ArbeidsfordelingResponse("4806", "Drammen", "Aktiv", "FPY");
        String json = WRITER.writeValueAsString(respons);
        ArbeidsfordelingResponse roundTripped = READER.forType(ArbeidsfordelingResponse.class).readValue(json);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getEnhetNr()).isEqualTo("4806");
    }
}
