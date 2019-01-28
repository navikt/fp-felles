package no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient;

import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse;

public interface ArbeidsfordelingConsumer {

    FinnBehandlendeEnhetListeResponse finnBehandlendeEnhetListe(FinnBehandlendeEnhetListeRequest request)
            throws FinnBehandlendeEnhetListeUgyldigInput;

    FinnAlleBehandlendeEnheterListeResponse finnAlleBehandlendeEnheterListe(FinnAlleBehandlendeEnheterListeRequest request)
            throws FinnAlleBehandlendeEnheterListeUgyldigInput;

}
