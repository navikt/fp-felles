package no.nav.vedtak.felles.integrasjon.spokelse;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public interface Spøkelse {

    List<SykepengeVedtak> hentGrunnlag(String fnr);

    List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom);

    List<SykepengeVedtak> hentGrunnlag(String fnr, LocalDate fom, Duration timeout);

    // Logger evt feil som info og gir tom list
    List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr);

    // Logger evt feil som info og gir tom list
    List<SykepengeVedtak> hentGrunnlagFailSoft(String fnr, LocalDate fom);
}
