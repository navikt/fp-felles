package no.nav.vedtak.felles.integrasjon.organisasjon;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EregRestTest {

    private static final String json = """
        {
          "organisasjonsnummer": "990983666",
          "type": "Virksomhet",
          "navn": {
            "redigertnavn": "NAV IKT",
            "navnelinje1": "NAV IKT",
            "bruksperiode": {
              "fom": "2015-02-23T08:04:53.2"
            },
            "gyldighetsperiode": {
              "fom": "2010-04-09"
            }
          },
          "organisasjonDetaljer": {
            "registreringsdato": "2007-03-05T00:00:00",
            "opphoersdato": "2018-11-06",
            "enhetstyper": [
              {
                "enhetstype": "BEDR",
                "bruksperiode": {
                  "fom": "2018-11-07T04:02:27.436"
                },
                "gyldighetsperiode": {
                  "fom": "2007-03-05"
                }
              }
            ],
            "naeringer": [
              {
                "naeringskode": "84.300",
                "hjelpeenhet": false,
                "bruksperiode": {
                  "fom": "2014-05-22T01:18:10.661"
                },
                "gyldighetsperiode": {
                  "fom": "2006-07-01"
                }
              }
            ],
            "navn": [
              {
                "redigertnavn": "NAV IKT",
                "navnelinje1": "NAV IKT",
                "bruksperiode": {
                  "fom": "2015-02-23T08:04:53.2"
                },
                "gyldighetsperiode": {
                  "fom": "2010-04-09"
                }
              }
            ],
            "forretningsadresser": [
              {
                "type": "Forretningsadresse",
                "adresselinje1": "Sannergata 2",
                "postnummer": "0557",
                "landkode": "NO",
                "kommunenummer": "0301",
                "bruksperiode": {
                  "fom": "2015-02-23T10:38:34.403"
                },
                "gyldighetsperiode": {
                  "fom": "2007-08-23"
                }
              }
            ],
            "postadresser": [
              {
                "type": "Postadresse",
                "adresselinje1": "Postboks 5 St Olavs plass",
                "postnummer": "0130",
                "landkode": "NO",
                "kommunenummer": "0301",
                "bruksperiode": {
                  "fom": "2015-02-23T10:38:34.403"
                },
                "gyldighetsperiode": {
                  "fom": "2010-10-08"
                }
              }
            ],
            "sistEndret": "2014-02-17"
          },
          "virksomhetDetaljer": {
            "enhetstype": "BEDR",
            "oppstartsdato": "2006-07-01"
          }
        }
        """;

    @Test
    void mapping_organisasjon() {
        var org = DefaultJsonMapper.fromJson(json, OrganisasjonEReg.class);

        assertThat(org.getNavn()).isEqualTo("NAV IKT");
        assertThat(org.type()).isEqualTo(OrganisasjonstypeEReg.VIRKSOMHET);
    }

}
