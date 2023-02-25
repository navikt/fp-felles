package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class OppgaveRestTest {

    private static final String json = """
        {
          "id": 357736794,
          "tildeltEnhetsnr":"1234",
          "opprettetAvEnhetsnr":"1234",
          "saksreferanse":"152200771",
          "aktoerId":"1000000000000",
          "beskrivelse":"Må behandle sak i VL!",
          "temagruppe":"FMLI",
          "tema":"FOR",
          "behandlingstema":"ab0326",
          "oppgavetype":"BEH_SAK",
          "versjon":1,
          "opprettetAv":"srvengangsstonad",
          "prioritet":"NORM",
          "status": "OPPRETTET",
          "metadata":{},
          "fristFerdigstillelse":"2023-01-23",
          "aktivDato":"2023-01-22",
          "opprettetTidspunkt":"2023-01-22T18:52:45.206+01:00"
        }
        """;

    @Test
    void test_response() throws Exception {
        var deserialized = DefaultJsonMapper.fromJson(json, Oppgave.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.tildeltEnhetsnr()).isEqualTo("1234");
        assertThat(deserialized.oppgavetype()).isEqualTo(Oppgavetype.BEHANDLE_SAK.getKode());
    }
}
