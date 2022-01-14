package no.nav.vedtak.felles.integrasjon.spokelse;

import java.util.List;

public interface Spøkelse {

    List<SykepengeVedtak> hentGrunnlag(String fnr);

    // Logger evt feil som info og gir tom list
    List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr);
}
