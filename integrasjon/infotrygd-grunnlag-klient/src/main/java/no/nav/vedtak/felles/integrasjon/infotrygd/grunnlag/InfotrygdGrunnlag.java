package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag;

import java.time.LocalDate;
import java.util.List;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

public interface InfotrygdGrunnlag {

    List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom);

    List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom);

}
