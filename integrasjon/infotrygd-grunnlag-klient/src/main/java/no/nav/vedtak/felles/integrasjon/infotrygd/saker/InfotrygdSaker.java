package no.nav.vedtak.felles.integrasjon.infotrygd.saker;

import java.util.List;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.GrunnlagRequest;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.InfotrygdSak;

public interface InfotrygdSaker {

    List<InfotrygdSak> hentSaker(GrunnlagRequest request);

    List<InfotrygdSak> hentSakerFailSoft(GrunnlagRequest request);

}
