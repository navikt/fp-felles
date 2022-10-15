package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.StatusKode;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class DeserializationTest {

    private static final String SAMPLE = """
        [ {
          "fødselsdatoBarn" : null,
          "kategori" : {
            "kode" : "01",
            "termnavn" : "Arbeidstaker"
          },
          "opphørFom" : "2022-10-17",
          "status" : {
            "kode" : "L",
            "termnavn" : "Løpende"
          },
          "tema" : {
            "kode" : "SP",
            "termnavn" : "Sykepenger"
          },
          "dekningsgrad" : null,
          "arbeidsforhold" : [ {
            "orgnr" : "888888888",
            "inntekt" : 654321,
            "inntektsperiode" : {
              "kode" : "Å",
              "termnavn" : "Årlig"
            },
            "refusjon" : true,
            "refusjonTom" : null
          } ],
          "periode" : {
            "fom" : "2022-08-31",
            "tom" : "2022-09-29"
          },
          "behandlingstema" : {
            "kode" : "SP",
            "termnavn" : "Sykepenger"
          },
          "identdato" : "2022-08-15",
          "iverksatt" : "2022-08-15",
          "gradering" : null,
          "opprinneligIdentdato" : null,
          "registrert" : "2022-08-15",
          "saksbehandlerId" : "SBH0000",
          "vedtak" : [ {
            "periode" : {
              "fom" : "2022-08-31",
              "tom" : "2022-09-29"
            },
            "utbetalingsgrad" : 100,
            "arbeidsgiverOrgnr" : "888888888",
            "erRefusjon" : true,
            "dagsats" : 1999
          } ]
        }, {
          "fødselsdatoBarn" : null,
          "kategori" : {
            "kode" : "01",
            "termnavn" : "Arbeidstaker"
          },
          "opphørFom" : "2021-02-08",
          "status" : {
            "kode" : "I",
            "termnavn" : "Ikke startet"
          },
          "tema" : {
            "kode" : "SP",
            "termnavn" : "Sykepenger"
          },
          "dekningsgrad" : null,
          "arbeidsforhold" : [ ],
          "periode" : null,
          "behandlingstema" : {
            "kode" : "SP",
            "termnavn" : "Sykepenger"
          },
          "identdato" : "2021-01-05",
          "iverksatt" : "2021-01-05",
          "gradering" : null,
          "opprinneligIdentdato" : null,
          "registrert" : "2021-01-05",
          "saksbehandlerId" : "AUTO",
          "vedtak" : [ ]
        } ]
        """;

    @Test
    void test_utbetalt()  {
        var grunnlagene = Arrays.asList(DefaultJsonMapper.fromJson(SAMPLE, Grunnlag[].class));
        var grunnlag = grunnlagene.get(0);
        assertThat(grunnlag.status().kode()).isEqualTo(StatusKode.L);
        assertThat(grunnlag.arbeidsforhold().get(0).orgnr().orgnr()).isEqualTo("888888888");
        assertThat(grunnlag.identdato()).isEqualTo(LocalDate.of(2022, 8, 15));
        assertThat(grunnlag.vedtak().get(0).utbetalingsgrad()).isEqualTo(100);
        assertThat(grunnlag.vedtak().get(0).arbeidsgiverOrgnr()).isEqualTo("888888888");
        assertThat(grunnlag.dekningsgrad()).isNull();
        assertThat(grunnlagene.get(1).status().kode()).isEqualTo(StatusKode.I);
    }




}
