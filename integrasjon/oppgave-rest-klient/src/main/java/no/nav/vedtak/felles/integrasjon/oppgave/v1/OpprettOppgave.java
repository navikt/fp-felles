package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpprettOppgave(String temagruppe, String tema, String behandlingstema, String behandlingstype, String behandlesAvApplikasjon,
                             Oppgavetype oppgavetype, String beskrivelse, String saksreferanse, String aktoerId, String journalpostId,
                             String opprettetAvEnhetsnr, String tildeltEnhetsnr, Prioritet prioritet, LocalDate aktivDato,
                             LocalDate fristFerdigstillelse) {

    public static final String TEMAGRUPPE_FAMILIEYTELSER = "FMLI";
    public static final String TEMA_FORELDREPENGER = "FOR";


    public static Builder getBuilder(Oppgavetype type, Prioritet prioritet, int fristDager) {
        var iDag = LocalDate.now();
        return new Builder().medOppgavetype(type)
            .medPrioritet(prioritet)
            .medAktivDato(LocalDate.now())
            .medFristFerdigstillelse(helgeJustertFrist(iDag.plusDays(fristDager)));
    }

    public static Builder getBuilderTemaFOR(Oppgavetype type, Prioritet prioritet, int fristDager) {
        return getBuilder(type, prioritet, fristDager).medTemagruppe(TEMAGRUPPE_FAMILIEYTELSER).medTema(TEMA_FORELDREPENGER);
    }

    private static LocalDate helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays((1L + DayOfWeek.SUNDAY.getValue()) - dato.getDayOfWeek().getValue());
        }
        return dato;
    }

    public static class Builder {
        private String tildeltEnhetsnr;
        private String opprettetAvEnhetsnr;
        private String journalpostId;
        private String behandlesAvApplikasjon;
        private String saksreferanse;
        private String aktoerId;
        private String beskrivelse;
        private String temagruppe;
        private String tema;
        private String behandlingstema;
        private Oppgavetype oppgavetype;
        private String behandlingstype;
        private LocalDate aktivDato;
        private Prioritet prioritet;
        private LocalDate fristFerdigstillelse;

        private Builder() {
        }

        public Builder medTildeltEnhetsnr(String tildeltEnhetsnr) {
            this.tildeltEnhetsnr = tildeltEnhetsnr;
            return this;
        }

        public Builder medOpprettetAvEnhetsnr(String opprettetAvEnhetsnr) {
            this.opprettetAvEnhetsnr = opprettetAvEnhetsnr;
            return this;
        }

        public Builder medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder medBehandlesAvApplikasjon(String behandlesAvApplikasjon) {
            this.behandlesAvApplikasjon = behandlesAvApplikasjon;
            return this;
        }

        public Builder medSaksreferanse(String saksreferanse) {
            this.saksreferanse = saksreferanse;
            return this;
        }

        public Builder medAktoerId(String aktoerId) {
            this.aktoerId = aktoerId;
            return this;
        }

        public Builder medBeskrivelse(String beskrivelse) {
            this.beskrivelse = beskrivelse;
            return this;
        }

        public Builder medTemagruppe(String temagruppe) {
            this.temagruppe = temagruppe;
            return this;
        }

        public Builder medTema(String tema) {
            this.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(String behandlingstema) {
            this.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medBehandlingstype(String behandlingstype) {
            this.behandlingstype = behandlingstype;
            return this;
        }

        private Builder medOppgavetype(Oppgavetype oppgavetype) {
            this.oppgavetype = oppgavetype;
            return this;
        }

        private Builder medAktivDato(LocalDate aktivDato) {
            this.aktivDato = aktivDato;
            return this;
        }

        private Builder medFristFerdigstillelse(LocalDate fristFerdigstillelse) {
            this.fristFerdigstillelse = fristFerdigstillelse;
            return this;
        }

        private Builder medPrioritet(Prioritet prioritet) {
            this.prioritet = prioritet;
            return this;
        }

        public OpprettOppgave build() {
            return new OpprettOppgave(temagruppe, tema, behandlingstema, behandlingstype, behandlesAvApplikasjon, oppgavetype, beskrivelse,
                saksreferanse, aktoerId, journalpostId, opprettetAvEnhetsnr, tildeltEnhetsnr, prioritet, aktivDato, fristFerdigstillelse);
        }


    }

}
