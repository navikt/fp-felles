package no.nav.vedtak.felles.integrasjon.arbeidsfordeling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class ArbeidsfordelingRestTest {

    private static final ObjectWriter WRITER = DefaultJsonMapper.getObjectMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = DefaultJsonMapper.getObjectMapper().reader();

    @Test
    void test_request() {
        var request = ArbeidsfordelingRequest.ny().medTema("FOR").medOppgavetype("BEH_SAK_VL").medBehandlingstype("ae0028").build();

        var json = DefaultJsonMapper.toJson(request);
        ArbeidsfordelingRequest roundTripped = DefaultJsonMapper.fromJson(json, ArbeidsfordelingRequest.class);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.tema()).isEqualTo("FOR");
        assertThat(roundTripped.behandlingstype()).isEqualTo("ae0028");
    }

    @Test
    void test_response() throws Exception {
        var respons = new ArbeidsfordelingResponse("4806", "Drammen", "Aktiv", "FPY");
        var json = WRITER.writeValueAsString(respons);
        ArbeidsfordelingResponse roundTripped = DefaultJsonMapper.fromJson(json, ArbeidsfordelingResponse.class);
        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.enhetNr()).isEqualTo("4806");
    }
}
