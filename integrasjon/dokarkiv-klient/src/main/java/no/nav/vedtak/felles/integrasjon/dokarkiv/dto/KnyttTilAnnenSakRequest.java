package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.List;

// Bruk tom liste for dokumenter for å inkludere alle, ellers angi en liste av dokumentId som kopieres
public record KnyttTilAnnenSakRequest(String sakstype, String fagsakId, String fagsaksystem,
                                      Bruker bruker, String tema, String journalfoerendeEnhet, List<Long> dokumenter) {

    public KnyttTilAnnenSakRequest(String sakstype, String fagsakId, String fagsaksystem,
                                   Bruker bruker, String tema, String journalfoerendeEnhet) {
        this(sakstype, fagsakId, fagsaksystem, bruker, tema, journalfoerendeEnhet, List.of());
    }


}
