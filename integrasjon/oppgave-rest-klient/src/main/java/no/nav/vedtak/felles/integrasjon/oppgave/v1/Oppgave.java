package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Oppgave(Long id,
                      String journalpostId,
                      String behandlesAvApplikasjon,
                      String saksreferanse,
                      String aktoerId,
                      String tema,
                      String behandlingstema,
                      String oppgavetype,
                      String behandlingstype,
                      Integer versjon,
                      String tildeltEnhetsnr,
                      LocalDate fristFerdigstillelse,
                      LocalDate aktivDato,
                      Prioritet prioritet,
                      Oppgavestatus status,
                      String beskrivelse,
                      String tilordnetRessurs) {

}
