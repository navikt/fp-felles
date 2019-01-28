package no.nav.vedtak.felles.integrasjon.behandlejournal;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;

public interface BehandleJournalConsumer {

    JournalfoerNotatResponse journalfoerNotat(JournalfoerNotatRequest request) throws JournalfoerNotatSikkerhetsbegrensning;
}
