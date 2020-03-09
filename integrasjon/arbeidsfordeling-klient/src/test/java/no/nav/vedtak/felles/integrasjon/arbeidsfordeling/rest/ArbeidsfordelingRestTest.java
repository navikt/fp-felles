package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ArbeidsfordelingRestTest {

    private static final ObjectWriter WRITER = TestJsonMapper.getMapper().writerWithDefaultPrettyPrinter();
    private static final ObjectReader READER = TestJsonMapper.getMapper().reader();

    @Test
    public void test_request() throws Exception {
        var request = ArbeidsfordelingRequest.ny()
            .medTema("FOR")
            .medOppgavetype("BEH_SAK_VL")
            .medBehandlingstype("ae0028")
            .build();

        String json = WRITER.writeValueAsString(request);
        System.out.println(json);

        ArbeidsfordelingRequest roundTripped = READER.forType(ArbeidsfordelingRequest.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getTema()).isEqualTo("FOR");
        assertThat(roundTripped.getBehandlingstype()).isEqualTo("ae0028");
    }

    @Test
    public void  test_response() throws Exception {
        var respons = new ArbeidsfordelingResponse("4806", "Drammen", "Aktiv", "FPY");

        String json = WRITER.writeValueAsString(respons);
        System.out.println(json);

        ArbeidsfordelingResponse roundTripped = READER.forType(ArbeidsfordelingResponse.class).readValue(json);

        assertThat(roundTripped).isNotNull();
        assertThat(roundTripped.getEnhetNr()).isEqualTo("4806");
    }
}
