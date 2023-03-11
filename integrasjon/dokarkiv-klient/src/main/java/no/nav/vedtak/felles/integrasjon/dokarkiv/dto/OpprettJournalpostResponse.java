package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.List;

public record OpprettJournalpostResponse(String journalpostId, boolean journalpostferdigstilt, List<DokumentInfoResponse> dokumenter) {

    public record DokumentInfoResponse(String dokumentInfoId) {
    }
}
