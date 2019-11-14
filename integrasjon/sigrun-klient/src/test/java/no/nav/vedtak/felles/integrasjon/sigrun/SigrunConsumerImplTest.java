package no.nav.vedtak.felles.integrasjon.sigrun;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGGrunnlag;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGResponse;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;

public class SigrunConsumerImplTest {

    private static final long AKTØR_ID = 123123L;
    private SigrunRestClient client = Mockito.mock(SigrunRestClient.class);

    private SigrunConsumer consumer = new SigrunConsumerImpl(client, null);

    private String JSON = "[\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektFiskeFangstFamiliebarnehage\",\n" +
        "    \"verdi\": \"814952\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektNaering\",\n" +
        "    \"verdi\": \"785896\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektBarePensjonsdel\",\n" +
        "    \"verdi\": \"844157\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"svalbardLoennLoennstrekkordningen\",\n" +
        "    \"verdi\": \"874869\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektLoenn\",\n" +
        "    \"verdi\": \"746315\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"svalbardPersoninntektNaering\",\n" +
        "    \"verdi\": \"696009\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"skatteoppgjoersdato\",\n" +
        "    \"verdi\": \"2017-08-09\"\n" +
        "  }\n" +
        "]";


    private String JSON_uten_skatteoppgjoersdato = "[\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektFiskeFangstFamiliebarnehage\",\n" +
        "    \"verdi\": \"814952\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektNaering\",\n" +
        "    \"verdi\": \"785896\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektBarePensjonsdel\",\n" +
        "    \"verdi\": \"844157\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"svalbardLoennLoennstrekkordningen\",\n" +
        "    \"verdi\": \"874869\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"personinntektLoenn\",\n" +
        "    \"verdi\": \"746315\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"tekniskNavn\": \"svalbardPersoninntektNaering\",\n" +
        "    \"verdi\": \"696009\"\n" +
        "  }\n" +
        "]";

    String JSON_summerskattegrunnlag = "{\n" +
        "  \"grunnlag\": [\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
        "      \"beloep\": 779981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"andreFradragsberettigedeKostnader\",\n" +
        "      \"beloep\": 59981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"samletSkattepliktigOverskuddAvUtleieAvFritidseiendom\",\n" +
        "      \"beloep\": 1609981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
        "      \"beloep\": 1749981\n" +
        "    }\n" +
        "  ],\n" +
        "  \"skatteoppgjoersdato\": \"2018-10-04\",\n" +
        "  \"svalbardGrunnlag\": [\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"samledePaaloepteRenter\",\n" +
        "      \"beloep\": 779981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"samletAndelAvInntektIBoligselskapEllerBoligsameie\",\n" +
        "      \"beloep\": 849981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen\",\n" +
        "      \"beloep\": 1779981\n" +
        "    },\n" +
        "    {\n" +
        "      \"tekniskNavn\": \"skattepliktigAvkastningEllerKundeutbytte\",\n" +
        "      \"beloep\": 1749981\n" +
        "    }\n" +
        "  ]\n" +
        "}\n";

    @Test
    public void skal_hente_og_mappe_om_data_fra_sigrun() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString())).thenReturn(JSON);

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID);
        assertThat(beregnetskatt.getBeregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.getBeregnetSkatt().get(iFjor)).hasSize(7);
        assertThat(beregnetskatt.getBeregnetSkatt().get(toÅrSiden)).hasSize(0);
        assertThat(beregnetskatt.getBeregnetSkatt().get(treÅrSiden)).hasSize(0);
    }

    @Test
    public void skal_hente_data_for_forifjor_når_skatteoppgjoersdato_mangler_for_ifjor() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);
        Year fireÅrSiden = Year.now().minusYears(4L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString())).thenReturn(JSON_uten_skatteoppgjoersdato);
        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, toÅrSiden.toString())).thenReturn(JSON);

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID);
        assertThat(beregnetskatt.getBeregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.getBeregnetSkatt().get(iFjor)).isNull();
        assertThat(beregnetskatt.getBeregnetSkatt().get(toÅrSiden)).hasSize(7);
        assertThat(beregnetskatt.getBeregnetSkatt().get(treÅrSiden)).hasSize(0);
        assertThat(beregnetskatt.getBeregnetSkatt().get(fireÅrSiden)).hasSize(0);
    }

    @Test
    public void skal_hente_summertskattegrunnlag() {
        Year iFjor = Year.now().minusYears(1L);

        Mockito.when(client.hentSummertskattegrunnlag(AKTØR_ID, iFjor.toString())).thenReturn(JSON_summerskattegrunnlag);

        SigrunSummertSkattegrunnlagResponse response = consumer.summertSkattegrunnlag(AKTØR_ID);

        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = response.getSummertskattegrunnlagMap();
        Optional<SSGResponse> sum = summertskattegrunnlagMap.get(iFjor);
        assertThat(sum).isPresent();
        SSGResponse ssgResponse = sum.get();
        assertThat(ssgResponse.getGrunnlag()).hasSize(4);
        assertThat(ssgResponse.getSvalbardGrunnlag()).hasSize(4);
        assertThat(ssgResponse.getSkatteoppgjoersdato()).isNotEmpty();
    }
}
