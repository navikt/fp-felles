package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TilknyttVedleggRequest(List<DokumentTilknytt> dokument) {

    public static record DokumentTilknytt(String kildeJournalpostId, String dokumentInfoId) {
    }
}
