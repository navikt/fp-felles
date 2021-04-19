package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;

class XacmlResponseTest {

    @Test
    public void createResponse() throws Exception {
        var mapper = DefaultJsonMapper.MAPPER;
        var deserialized = mapper.readValue(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("xacmlresponse.json")).getFile()), XacmlResponse.class);

        var serialized = mapper.writeValueAsString(deserialized);

        assertThat(deserialized.Response()).isNotEmpty();
        assertThat(deserialized.Response()).hasSize(1);
    }

}
