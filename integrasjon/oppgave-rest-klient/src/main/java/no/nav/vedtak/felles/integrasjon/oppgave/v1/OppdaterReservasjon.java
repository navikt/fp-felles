package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude()
public record OppdaterReservasjon(Long id, Integer versjon, String tilordnetRessurs) {
}
