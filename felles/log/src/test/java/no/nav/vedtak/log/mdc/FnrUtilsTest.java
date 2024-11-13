package no.nav.vedtak.log.mdc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FnrUtilsTest {

    @Test
    void maskerFnrTest() {
        var fdato = "122314";
        var pnr = "55555";
        var fakeTestFnr = fdato + pnr;
        var maskertFnr = FnrUtils.maskFnr(fakeTestFnr);
        assertThat(maskertFnr).isNotBlank().startsWith(fdato).doesNotContain(pnr).endsWith("*****");
    }

    @Test
    void ikkeMaskerVanligTekst() {
        var ikkeFnr = "dev-fss:teamforeldrepenger:fpsak";
        var ikkeMaskert = FnrUtils.maskFnr(ikkeFnr);
        assertThat(ikkeMaskert).isEqualTo(ikkeFnr);
    }

    @Test
    void ikkeMaskerOrgnr() {
        var fakeOrgnr9siffer = "123456789";
        var ikkeMaskert = FnrUtils.maskFnr(fakeOrgnr9siffer);
        assertThat(ikkeMaskert).isEqualTo(fakeOrgnr9siffer);
    }
}
