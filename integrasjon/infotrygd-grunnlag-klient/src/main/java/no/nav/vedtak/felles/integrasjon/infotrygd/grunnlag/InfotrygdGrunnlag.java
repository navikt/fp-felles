package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag;

import java.util.List;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

public interface InfotrygdGrunnlag {


    /*
     * POST
     */
    List<Grunnlag> hentGrunnlag(GrunnlagRequest request);

    List<Grunnlag> hentGrunnlagFailSoft(GrunnlagRequest request);

}
