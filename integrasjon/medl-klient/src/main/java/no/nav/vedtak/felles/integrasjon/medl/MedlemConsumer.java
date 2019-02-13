package no.nav.vedtak.felles.integrasjon.medl;

import no.nav.tjeneste.virksomhet.medlemskap.v2.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.medlemskap.v2.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeResponse;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeResponse;

public interface MedlemConsumer {

    HentPeriodeResponse hentPeriode(HentPeriodeRequest hentPeriodeRequest) throws Sikkerhetsbegrensning;

    HentPeriodeListeResponse hentPeriodeListe(HentPeriodeListeRequest hentPeriodeListeRequest) throws PersonIkkeFunnet, Sikkerhetsbegrensning;

}
