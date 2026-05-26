package no.nav.vedtak.util;

import static no.nav.vedtak.util.InputValideringRegex.ADRESSE;
import static no.nav.vedtak.util.InputValideringRegex.ARBEIDSGIVER;
import static no.nav.vedtak.util.InputValideringRegex.FRITEKST;
import static no.nav.vedtak.util.InputValideringRegex.KODEVERK;
import static no.nav.vedtak.util.InputValideringRegex.NAVN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class InputValideringRegexTest {

    private static final Pattern NAVN_PATTERN = Pattern.compile(NAVN);
    private static final Pattern ADRESSE_PATTERN = Pattern.compile(ADRESSE);
    private static final Pattern AG_PATTERN = Pattern.compile(ARBEIDSGIVER);
    private static final Pattern FRITEKST_PATTERN = Pattern.compile(FRITEKST);
    private static final Pattern KODEVERK_PATTERN = Pattern.compile(KODEVERK);

    @Test
    void skal_matche_ulike_navn() {
        assertThat(NAVN_PATTERN.matcher("Gisle-Børge").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Kari Normann").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Mc'Donald").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Dr. Know Jr.").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Günther").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Åsne").matches()).isTrue();

        // for å enklere teste, bør antagelig fjernes
        assertThat(NAVN_PATTERN.matcher("Andersen Syntetisk 134").matches()).isFalse();

        // samisk navn
        assertThat(NAVN_PATTERN.matcher("Áigesárri").matches()).isTrue();
        assertThat(NAVN_PATTERN.matcher("Bážá").matches()).isTrue();

        // Polsk bonanza
        assertThat(NAVN_PATTERN.matcher("Zażółć gęślą jaźń").matches()).isTrue();
    }

    @Test
    void skal_ikke_tillate_diverse_som_navn() {
        assertThat(NAVN_PATTERN.matcher("<script type=js").matches()).isFalse();
        assertThat(NAVN_PATTERN.matcher("\\u0013rf").matches()).isFalse();
    }

    @Test
    void skal_matche_adreser() {
        assertThat(ADRESSE_PATTERN.matcher("Kari Normann\n\tParkveien 1\n0141 OSLO").matches()).isTrue();
        assertThat(ADRESSE_PATTERN.matcher("Mc'Donald\nc/o Kari Normann\nParkveien 1\n4124 Bærum verk\nNORGE").matches()).isTrue();
    }

    @Test
    void skal_ikke_tillate_script_tegn_i_noen_av_regexene() {
        assertThat(KODEVERK_PATTERN.matcher("<script type=js>").matches()).isFalse();
        assertThat(NAVN_PATTERN.matcher("<script type=js>").matches()).isFalse();
        assertThat(ADRESSE_PATTERN.matcher("<script type=js>").matches()).isFalse();
        assertThat(FRITEKST_PATTERN.matcher("<script type=js>").matches()).isFalse();
    }

    @Test
    void skal_matche_fritekst() {
        assertThat(FRITEKST_PATTERN.matcher("Pga. §124 i Lov om foobar: \"sitat\", innvilges stønad. Se https://nav.no/abc/ for mer info.").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("Du har søkt om 80% stønadsgrad, og annen forelder om 100%; hva er riktig? Omforen dere!").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("Dette er (nesten) helt OK.").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("Send svar til meg@nav.no").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("Husk at 1+1=2").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("&").matches()).isTrue();
        assertThat(FRITEKST_PATTERN.matcher("%§\\!?@_()+:;,=\"&\\p{Sc}").matches()).isTrue();
    }

    @Test
    void skal_ikke_tillate_script_tag_i_fritekst() {
        assertThat(FRITEKST_PATTERN.matcher("<script").matches()).isFalse();
    }

    @Test
    void skal_matche_kodeverk() {
        assertThat(KODEVERK_PATTERN.matcher("ABC_123").matches()).isTrue();
        assertThat(KODEVERK_PATTERN.matcher("avbrutt-annulert").matches()).isTrue();
        assertThat(KODEVERK_PATTERN.matcher("ab0053").matches()).isTrue();
        assertThat(KODEVERK_PATTERN.matcher("NB").matches()).isTrue();
        assertThat(KODEVERK_PATTERN.matcher("æøåÆØÅ_214").matches()).isTrue();
    }

    @Test
    void arbeidsgiver_matcher_kun_13_og_9_digits() {
        assertThat(AG_PATTERN.matcher("123456789").matches()).isTrue();
        assertThat(AG_PATTERN.matcher("aaaaaaaaa").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("1234567891").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("a123456789").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("123456789a").matches()).isFalse();

        assertThat(AG_PATTERN.matcher("1234567890123").matches()).isTrue();
        assertThat(AG_PATTERN.matcher("aaaaaaaaaaaaa").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("12345678901234").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("a1234567890123").matches()).isFalse();
        assertThat(AG_PATTERN.matcher("1234567890123a").matches()).isFalse();
    }
}
