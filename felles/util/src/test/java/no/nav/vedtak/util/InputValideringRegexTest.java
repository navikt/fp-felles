package no.nav.vedtak.util;

import org.junit.Test;

import static no.nav.vedtak.util.InputValideringRegex.ADRESSE;
import static no.nav.vedtak.util.InputValideringRegex.FRITEKST;
import static no.nav.vedtak.util.InputValideringRegex.NAVN;
import static no.nav.vedtak.util.InputValideringRegex.KODEVERK;
import static org.assertj.core.api.Assertions.assertThat;

public class InputValideringRegexTest {

    @Test
    public void skal_matche_ulike_navn() throws Exception {
        assertThat("Gisle-Børge").matches(NAVN);
        assertThat("Kari Normann").matches(NAVN);
        assertThat("Mc'Donald").matches(NAVN);
        assertThat("Dr. Know Jr.").matches(NAVN);
        assertThat("Günther").matches(NAVN);
        assertThat("Åsne").matches(NAVN);

        //for å enklere teste, bør antagelig fjernes
        assertThat("Andersen Syntetisk 134").matches(NAVN);

        //samisk navn
        assertThat("Áigesárri").matches(NAVN);
        assertThat("Bážá").matches(NAVN);
    }

    @Test
    public void skal_ikke_tillate_diverse_som_navn() throws Exception {
        assertThat("<script type=js").doesNotMatch(NAVN);
        assertThat("\\u0013rf").doesNotMatch(NAVN);
    }

    @Test
    public void skal_matche_adreser() throws Exception {
        assertThat("Kari Normann\nParkveien 1\n0141 OSLO").matches(ADRESSE);
        assertThat("Mc'Donald\nc/o Kari Normann\nParkveien 1\n4124 Bærum verk\nNORGE").matches(ADRESSE);
    }

    @Test
    public void skal_ikke_tillate_diverse_som_adresse() throws Exception {
        assertThat("<script type=js").doesNotMatch(ADRESSE);
        assertThat("\\u0013rf").doesNotMatch(ADRESSE);
    }

    @Test
    public void skal_matche_fritekst() throws Exception {
        assertThat("Pga. §124 i Lov om foobar: \"sitat\", innvilges stønad. Se https://nav.no/abc/ for mer info.").matches(FRITEKST);
        assertThat("Du har søkt om 80% stønadsgrad, og annen forelder om 100%; hva er riktig? Omforen dere!").matches(FRITEKST);
        assertThat("Dette er (nesten) helt OK.").matches(FRITEKST);
        assertThat("Send svar til meg@nav.no").matches(FRITEKST);
        assertThat("Husk at 1+1=2").matches(FRITEKST);
        assertThat("&").matches(FRITEKST);
    }

    @Test
    public void skal_ikke_tillate_script_tag_i_fritekst() throws Exception {
        assertThat("<script").doesNotMatch(FRITEKST);
    }

    @Test
    public void skal_matche_kodeverk() throws Exception {
        assertThat("ABC_123").matches(KODEVERK);
        assertThat("avbrutt-annulert").matches(KODEVERK);
        assertThat("ab0053").matches(KODEVERK);
        assertThat("NB").matches(KODEVERK);
        assertThat("æøåÆØÅ_214").matches(KODEVERK);
    }
}