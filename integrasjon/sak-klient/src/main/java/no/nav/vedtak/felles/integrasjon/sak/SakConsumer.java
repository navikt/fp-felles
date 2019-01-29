package no.nav.vedtak.felles.integrasjon.sak;

import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;

public interface SakConsumer {
    FinnSakResponse finnSak(FinnSakRequest request) throws FinnSakForMangeForekomster, FinnSakUgyldigInput;
}
