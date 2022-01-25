package no.nav.vedtak.felles.integrasjon.organisasjon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class EregRestTest {

    private static final String json = "{\n" +
            "  \"organisasjonsnummer\": \"990983666\",\n" +
            "  \"type\": \"Virksomhet\",\n" +
            "  \"navn\": {\n" +
            "    \"redigertnavn\": \"NAV IKT\",\n" +
            "    \"navnelinje1\": \"NAV IKT\",\n" +
            "    \"bruksperiode\": {\n" +
            "      \"fom\": \"2015-02-23T08:04:53.2\"\n" +
            "    },\n" +
            "    \"gyldighetsperiode\": {\n" +
            "      \"fom\": \"2010-04-09\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"organisasjonDetaljer\": {\n" +
            "    \"registreringsdato\": \"2007-03-05T00:00:00\",\n" +
            "    \"opphoersdato\": \"2018-11-06\",\n" +
            "    \"enhetstyper\": [\n" +
            "      {\n" +
            "        \"enhetstype\": \"BEDR\",\n" +
            "        \"bruksperiode\": {\n" +
            "          \"fom\": \"2018-11-07T04:02:27.436\"\n" +
            "        },\n" +
            "        \"gyldighetsperiode\": {\n" +
            "          \"fom\": \"2007-03-05\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"naeringer\": [\n" +
            "      {\n" +
            "        \"naeringskode\": \"84.300\",\n" +
            "        \"hjelpeenhet\": false,\n" +
            "        \"bruksperiode\": {\n" +
            "          \"fom\": \"2014-05-22T01:18:10.661\"\n" +
            "        },\n" +
            "        \"gyldighetsperiode\": {\n" +
            "          \"fom\": \"2006-07-01\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"navn\": [\n" +
            "      {\n" +
            "        \"redigertnavn\": \"NAV IKT\",\n" +
            "        \"navnelinje1\": \"NAV IKT\",\n" +
            "        \"bruksperiode\": {\n" +
            "          \"fom\": \"2015-02-23T08:04:53.2\"\n" +
            "        },\n" +
            "        \"gyldighetsperiode\": {\n" +
            "          \"fom\": \"2010-04-09\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"forretningsadresser\": [\n" +
            "      {\n" +
            "        \"type\": \"Forretningsadresse\",\n" +
            "        \"adresselinje1\": \"Sannergata 2\",\n" +
            "        \"postnummer\": \"0557\",\n" +
            "        \"landkode\": \"NO\",\n" +
            "        \"kommunenummer\": \"0301\",\n" +
            "        \"bruksperiode\": {\n" +
            "          \"fom\": \"2015-02-23T10:38:34.403\"\n" +
            "        },\n" +
            "        \"gyldighetsperiode\": {\n" +
            "          \"fom\": \"2007-08-23\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"postadresser\": [\n" +
            "      {\n" +
            "        \"type\": \"Postadresse\",\n" +
            "        \"adresselinje1\": \"Postboks 5 St Olavs plass\",\n" +
            "        \"postnummer\": \"0130\",\n" +
            "        \"landkode\": \"NO\",\n" +
            "        \"kommunenummer\": \"0301\",\n" +
            "        \"bruksperiode\": {\n" +
            "          \"fom\": \"2015-02-23T10:38:34.403\"\n" +
            "        },\n" +
            "        \"gyldighetsperiode\": {\n" +
            "          \"fom\": \"2010-10-08\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"sistEndret\": \"2014-02-17\"\n" +
            "  },\n" +
            "  \"virksomhetDetaljer\": {\n" +
            "    \"enhetstype\": \"BEDR\",\n" +
            "    \"oppstartsdato\": \"2006-07-01\"\n" +
            "  }\n" +
            "}";

    @Test
    void mapping_organisasjon() throws IOException {
        var org = DefaultJsonMapper.fromJson(json, OrganisasjonEReg.class);

        assertThat(org.getNavn()).isEqualTo("NAV IKT");
        assertThat(org.getType()).isEqualTo(OrganisasjonstypeEReg.VIRKSOMHET);
    }

    @Test
    void mapping_adresse() throws IOException {
        var org = DefaultJsonMapper.fromJson(json, OrganisasjonAdresse.class);

        assertThat(org.getNavn()).isEqualTo("NAV IKT");
        assertThat(org.getKorrespondanseadresse().adresselinje1()).isEqualTo("Postboks 5 St Olavs plass");
    }
}
