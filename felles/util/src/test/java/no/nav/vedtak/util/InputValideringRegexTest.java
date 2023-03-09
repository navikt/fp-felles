package no.nav.vedtak.util;

import org.junit.jupiter.api.Test;

import static no.nav.vedtak.util.InputValideringRegex.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InputValideringRegexTest {

    @Test
    void skal_matche_ulike_navn() {
        assertThat("Gisle-Børge").matches(NAVN);
        assertThat("Kari Normann").matches(NAVN);
        assertThat("Mc'Donald").matches(NAVN);
        assertThat("Dr. Know Jr.").matches(NAVN);
        assertThat("Günther").matches(NAVN);
        assertThat("Åsne").matches(NAVN);

        // for å enklere teste, bør antagelig fjernes
        assertThat("Andersen Syntetisk 134").doesNotMatch(NAVN);

        // samisk navn
        assertThat("Áigesárri").matches(NAVN);
        assertThat("Bážá").matches(NAVN);

        // Polsk bonanza
        assertThat("Zażółć gęślą jaźń").matches(NAVN);
    }

    @Test
    void skal_ikke_tillate_diverse_som_navn() {
        assertThat("<script type=js").doesNotMatch(NAVN);
        assertThat("\\u0013rf").doesNotMatch(NAVN);
    }

    @Test
    void skal_matche_adreser() {
        assertThat("Kari Normann\n\tParkveien 1\n0141 OSLO").matches(ADRESSE);
        assertThat("Mc'Donald\nc/o Kari Normann\nParkveien 1\n4124 Bærum verk\nNORGE").matches(ADRESSE);
    }

    @Test
    void skal_ikke_tillate_diverse_som_adresse() {
        assertThat("<script type=js").doesNotMatch(ADRESSE);
        assertThat("\\u0013rf").doesNotMatch(ADRESSE);
    }

    @Test
    void skal_matche_fritekst() {
        assertThat("Pga. §124 i Lov om foobar: \"sitat\", innvilges stønad. Se https://nav.no/abc/ for mer info.").matches(FRITEKST);
        assertThat("Du har søkt om 80% stønadsgrad, og annen forelder om 100%; hva er riktig? Omforen dere!").matches(FRITEKST);
        assertThat("Dette er (nesten) helt OK.").matches(FRITEKST);
        assertThat("Send svar til meg@nav.no").matches(FRITEKST);
        assertThat("Husk at 1+1=2").matches(FRITEKST);
        assertThat("&").matches(FRITEKST);
    }

    @Test
    void skal_ikke_tillate_script_tag_i_fritekst() {
        assertThat("<script").doesNotMatch(FRITEKST);
    }

    @Test
    void skal_matche_kodeverk() {
        assertThat("ABC_123").matches(KODEVERK);
        assertThat("avbrutt-annulert").matches(KODEVERK);
        assertThat("ab0053").matches(KODEVERK);
        assertThat("NB").matches(KODEVERK);
        assertThat("æøåÆØÅ_214").matches(KODEVERK);
    }

    @Test
    void arbeidsgiver_matcher_kun_13_og_9_digits() {
        assertThat("123456789").matches(ARBEIDSGIVER);
        assertThat("aaaaaaaaa").doesNotMatch(ARBEIDSGIVER);
        assertThat("1234567891").doesNotMatch(ARBEIDSGIVER);
        assertThat("a123456789").doesNotMatch(ARBEIDSGIVER);
        assertThat("123456789a").doesNotMatch(ARBEIDSGIVER);

        assertThat("1234567890123").matches(ARBEIDSGIVER);
        assertThat("aaaaaaaaaaaaa").doesNotMatch(ARBEIDSGIVER);
        assertThat("12345678901234").doesNotMatch(ARBEIDSGIVER);
        assertThat("a1234567890123").doesNotMatch(ARBEIDSGIVER);
        assertThat("1234567890123a").doesNotMatch(ARBEIDSGIVER);
    }
}
