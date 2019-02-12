package no.nav.vedtak.felles.integrasjon.inngaaendejournal;

import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;

public interface InngaaendeJournalConsumer {

    HentJournalpostResponse hentJournalpost(HentJournalpostRequest request)
            throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
            HentJournalpostSikkerhetsbegrensning, HentJournalpostUgyldigInput;

    UtledJournalfoeringsbehovResponse utledJournalfoeringsbehov(UtledJournalfoeringsbehovRequest request) throws UtledJournalfoeringsbehovSikkerhetsbegrensning, UtledJournalfoeringsbehovUgyldigInput, UtledJournalfoeringsbehovJournalpostKanIkkeBehandles, UtledJournalfoeringsbehovJournalpostIkkeFunnet, UtledJournalfoeringsbehovJournalpostIkkeInngaaende;

}
