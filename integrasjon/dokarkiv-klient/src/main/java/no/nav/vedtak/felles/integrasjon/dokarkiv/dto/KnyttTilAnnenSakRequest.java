package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.time.LocalDate;

public record KnyttTilAnnenSakRequest(String sakstype, String fagsakId, String fagsaksystem,
                                      Bruker bruker, String tema, String journalfoerendeEnhet) {

}
