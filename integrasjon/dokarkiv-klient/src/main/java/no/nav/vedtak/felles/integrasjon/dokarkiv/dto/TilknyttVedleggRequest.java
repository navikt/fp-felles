package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Tilknyttet = saksbehandler eller optional. Rekkefølge: Se DokumentInfoOpprett
@JsonIgnoreProperties(ignoreUnknown = true)
public record TilknyttVedleggRequest(List<DokumentTilknytt> dokument, String tilknyttetAvNavn) {

    public record DokumentTilknytt(String kildeJournalpostId, String dokumentInfoId, Integer rekkefoelge) {
    }
}
