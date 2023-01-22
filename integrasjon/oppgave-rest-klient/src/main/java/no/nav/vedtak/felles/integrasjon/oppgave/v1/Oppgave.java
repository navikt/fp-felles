package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

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
                      @JsonDeserialize(using = LocalOffsetDateTimeFormatter.class) LocalDateTime opprettetTidspunkt) {

    private static class LocalOffsetDateTimeFormatter extends LocalDateTimeDeserializer {
        public LocalOffsetDateTimeFormatter() {
            super(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

}
