package no.nav.vedtak.felles.integrasjon.infotrygdsak;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;

public interface InfotrygdSakConsumer {

    FinnSakListeResponse finnSakListe(FinnSakListeRequest finnSakListeRequest) throws FinnSakListePersonIkkeFunnet, FinnSakListeSikkerhetsbegrensning, FinnSakListeUgyldigInput;

}