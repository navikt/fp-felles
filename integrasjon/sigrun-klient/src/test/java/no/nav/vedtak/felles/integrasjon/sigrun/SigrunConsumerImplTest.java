package no.nav.vedtak.felles.integrasjon.sigrun;

import java.time.Year;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

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

    @Test
    public void skal_hente_og_mappe_om_data_fra_sigrun() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString())).thenReturn(JSON);

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt()).hasSize(3);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(iFjor)).hasSize(7);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(toÅrSiden)).hasSize(0);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(treÅrSiden)).hasSize(0);
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
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt()).hasSize(3);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(iFjor)).isNull();
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(toÅrSiden)).hasSize(7);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(treÅrSiden)).hasSize(0);
        Assertions.assertThat(beregnetskatt.getBeregnetSkatt().get(fireÅrSiden)).hasSize(0);
    }
}