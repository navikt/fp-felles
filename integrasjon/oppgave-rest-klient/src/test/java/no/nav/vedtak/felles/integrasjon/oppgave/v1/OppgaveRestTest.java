package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class OppgaveRestTest {

    private static final String JSON_SER = """
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
    void test_response() {
        var deserialized = DefaultJsonMapper.fromJson(JSON_SER, Oppgave.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.tildeltEnhetsnr()).isEqualTo("1234");
        assertThat(deserialized.oppgavetype()).isEqualTo(Oppgavetype.BEHANDLE_SAK);
    }

    @Test
    void test_response_ukjent_oppgave_type() {
        var deserialized = DefaultJsonMapper.fromJson(JSON_SER.replace("BEH_SAK", "EN_VILL_EN"), Oppgave.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.tildeltEnhetsnr()).isEqualTo("1234");
        assertThat(deserialized.oppgavetype()).isEqualTo(Oppgavetype.UKJENT);
    }

    @Test
    void test_response_vurder_dok() {
        var deserialized = DefaultJsonMapper.fromJson(JSON_SER.replace("BEH_SAK", "VUR"), Oppgave.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.tildeltEnhetsnr()).isEqualTo("1234");
        assertThat(deserialized.oppgavetype()).isEqualTo(Oppgavetype.VURDER_DOKUMENT);
    }

    @Test
    void test_response_vurder_konsekvens() {
        var deserialized = DefaultJsonMapper.fromJson(JSON_SER.replace("BEH_SAK", "VUR_KONS_YTE"), Oppgave.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.tildeltEnhetsnr()).isEqualTo("1234");
        assertThat(deserialized.oppgavetype()).isEqualTo(Oppgavetype.VURDER_KONSEKVENS_YTELSE);
    }
}
