package no.nav.vedtak.felles.integrasjon.behandlesak.klient;

import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakResponse;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSakEksistererAlleredeException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSUgyldigInputException;

public interface BehandleSakConsumer {
    WSOpprettSakResponse opprettSak(WSOpprettSakRequest request) throws WSSikkerhetsbegrensningException, WSSakEksistererAlleredeException, WSUgyldigInputException;
}
