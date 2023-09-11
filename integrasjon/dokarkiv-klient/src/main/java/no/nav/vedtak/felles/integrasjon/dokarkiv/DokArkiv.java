package no.nav.vedtak.felles.integrasjon.dokarkiv;


import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.KnyttTilAnnenSakRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.KnyttTilAnnenSakResponse;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.TilknyttVedleggRequest;

public interface DokArkiv {

    OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean ferdigstill);

    boolean ferdigstillJournalpost(String journalpostId, String enhet);

    boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request);

    void tilknyttVedlegg(TilknyttVedleggRequest request, String journalpostId);

    KnyttTilAnnenSakResponse knyttTilAnnenSak(String journalpostId, KnyttTilAnnenSakRequest request);
}
