package no.nav.vedtak.felles.integrasjon.pdlpip;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;



public class PersondataPipTest {

    private static final String PERSON_RESPONS = """
        {
          "person": {
            "adressebeskyttelse": [
              {
                "gradering": "STRENGT_FORTROLIG_UTLAND"
              }
            ],
            "foedsel": [
              {
                "foedselsdato": "1911-01-01"
              }
            ],
            "doedsfall": [
              {
                "doedsdato": "1977-07-07"
              }
            ],
            "familierelasjoner": [
              {
                "relatertPersonsIdent": "11223344550"
              }
            ]
          },
          "identer": {
            "identer": [
              {
                "ident": "1234567890123",
                "historisk": false,
                "gruppe": "AKTORID"
              },
              {
                "ident": "9876543210987",
                "historisk": true,
                "gruppe": "AKTORID"
              },
              {
                "ident": "12345678901",
                "historisk": false,
                "gruppe": "FOLKEREGISTERIDENT"
              },
              {
                "ident": "98765432109",
                "historisk": true,
                "gruppe": "FOLKEREGISTERIDENT"
              }
            ]
          },
          "geografiskTilknytning": {
            "gtType": null,
            "gtKommune": "kommune",
            "gtBydel": "bydel",
            "gtLand": "sørlandet",
            "regel": "42"
          }
        }
        """;

    private static final String PERSON_BOLK_RESPONS = """
        {
          "12345678901": {
            "person": {
              "adressebeskyttelse": [],
              "foedsel": [
                {
                  "foedselsdato": "1998-05-09"
                }
              ],
              "doedsfall": [
              ],
              "familierelasjoner": [
                {
                  "relatertPersonsIdent": "23456789012"
                },
                {
                  "relatertPersonsIdent": "34567890123"
                }
              ]
            },
            "identer": {
              "identer": [
                {
                  "ident": "2052090676205",
                  "historisk": false,
                  "gruppe": "AKTORID"
                },
                {
                  "ident": "12345678901",
                  "historisk": false,
                  "gruppe": "FOLKEREGISTERIDENT"
                }
              ]
            },
            "geografiskTilknytning": {
              "gtType": "KOMMUNE",
              "gtKommune": "4644",
              "gtBydel": null,
              "gtLand": null,
              "regel": "18"
            }
          },
          "10111111111": null
        }
        """;

    @Test
    void test_response() {
        var deserialized = DefaultJsonMapper.fromJson(PERSON_RESPONS, PersondataPipDto.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized.harStrengAdresseBeskyttelse()).isTrue();
        assertThat(deserialized.harAdresseBeskyttelse()).isTrue();
        assertThat(deserialized.harIkkeNasjonalTilknytning()).isTrue();
        assertThat(deserialized.harNasjonalTilknytning()).isFalse();
        assertThat(deserialized.erIkkeMyndig()).isFalse();
        assertThat(deserialized.aktørIdMedHistoriske()).hasSize(2);
        assertThat(deserialized.aktørId()).isEqualTo("1234567890123");
        assertThat(deserialized.personIdent()).isEqualTo("12345678901");
    }

    @Test
    void test_response_bolk() {
        var deserialized = DefaultJsonMapper.mapFromJson(PERSON_BOLK_RESPONS, PersondataPipDto.class);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized).isNotEmpty();
        assertThat(deserialized.keySet()).hasSize(2);
        assertThat(deserialized.values()).hasSize(2);
        assertThat(deserialized.get("10111111111")).isNull();;
        var element = deserialized.get("12345678901");
        assertThat(element.harStrengAdresseBeskyttelse()).isFalse();
        assertThat(element.harAdresseBeskyttelse()).isFalse();
        assertThat(element.harNasjonalTilknytning()).isTrue();
        assertThat(element.harIkkeNasjonalTilknytning()).isFalse();
        assertThat(element.erIkkeMyndig()).isFalse();
        assertThat(element.aktørIdMedHistoriske()).hasSize(1);
        assertThat(element.aktørId()).isEqualTo("2052090676205");
        assertThat(element.personIdent()).isEqualTo("12345678901");
    }

}
