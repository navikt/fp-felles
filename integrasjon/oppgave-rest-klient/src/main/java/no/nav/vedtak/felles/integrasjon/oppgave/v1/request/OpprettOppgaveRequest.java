package no.nav.vedtak.felles.integrasjon.oppgave.v1.request;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpprettOppgaveRequest {

    private String opprettetAvEnhetsnr; // Enhet
    private AktørId aktoerId; // AktørId
    private String journalpostId; // JournalpostId
    private String behandlesAvApplikasjon; // fagsystem
    private String saksreferanse; // Saksnummer
    private String beskrivelse; // Fritekst?
    private String tema; // Tema (kodeerk)
    private String behandlingstema; // Behandlingtema kodeverk
    private String oppgavetype; // Oppgavetype kodeverk SOKNAD_FOR_TIL,
    private String behandlingstype; // Behanlingtype (kodverk)
    private LocalDate aktivDato = LocalDate.now(); // Dato frist:2018-03-24,
    private LocalDate fristFerdigstillelse; // Dato frist:2018-03-24,
    private String prioritet;
    private Map<String, String> metadata;

    public OpprettOppgaveRequest() {
    }

    public String getOpprettetAvEnhetsnr() {
        return opprettetAvEnhetsnr;
    }

    public OpprettOppgaveRequest setOpprettetAvEnhetsnr(String opprettetAvEnhetsnr) {
        this.opprettetAvEnhetsnr = opprettetAvEnhetsnr;
        return this;
    }

    public AktørId getAktoerId() {
        return aktoerId;
    }

    public OpprettOppgaveRequest setAktoerId(AktørId aktoerId) {
        this.aktoerId = aktoerId;
        return this;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public OpprettOppgaveRequest setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
        return this;
    }

    public String getBehandlesAvApplikasjon() {
        return behandlesAvApplikasjon;
    }

    public OpprettOppgaveRequest setBehandlesAvApplikasjon(String behandlesAvApplikasjon) {
        this.behandlesAvApplikasjon = behandlesAvApplikasjon;
        return this;
    }

    public String getSaksreferanse() {
        return saksreferanse;
    }

    public OpprettOppgaveRequest setSaksreferanse(String saksreferanse) {
        this.saksreferanse = saksreferanse;
        return this;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public OpprettOppgaveRequest setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public String getTema() {
        return tema;
    }

    public OpprettOppgaveRequest setTema(String tema) {
        this.tema = tema;
        return this;
    }

    public OpprettOppgaveRequest setTema(Tema tema) {
        this.tema = tema.getKode();
        return this;
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public OpprettOppgaveRequest setBehandlingstema(String behandlingstema) {
        this.behandlingstema = behandlingstema;
        return this;
    }

    public String getOppgavetype() {
        return oppgavetype;
    }

    public OpprettOppgaveRequest setOppgavetype(String oppgavetype) {
        this.oppgavetype = oppgavetype;
        return this;
    }

    public OpprettOppgaveRequest setOppgavetype(OppgaveType oppgavetype) {
        Objects.requireNonNull(oppgavetype);
        this.oppgavetype = oppgavetype.getKode();
        return this;
    }

    public String getBehandlingstype() {
        return behandlingstype;
    }

    public OpprettOppgaveRequest setBehandlingstype(String behandlingstype) {
        this.behandlingstype = behandlingstype;
        return this;
    }

    public LocalDate getFristFerdigstillelse() {
        return fristFerdigstillelse;
    }

    public OpprettOppgaveRequest setFristFerdigstillelse(LocalDate fristFerdigstillelse) {
        Objects.requireNonNull(fristFerdigstillelse, "Frist kan ikke være null");
        this.fristFerdigstillelse = fristFerdigstillelse;
        return this;
    }

    public LocalDate getAktivDato() {
        return aktivDato;
    }

    public OpprettOppgaveRequest setAktivDato(LocalDate aktivDato) {
        Objects.requireNonNull(aktivDato, "Aktivdato kan ikke være null");
        this.aktivDato = aktivDato;
        return this;
    }

    public String getPrioritet() {
        return prioritet;
    }

    public OpprettOppgaveRequest setPrioritet(OppgavePrioritet prioritet) {
        Objects.requireNonNull(prioritet, "Prioritet kan ikke være null");
        this.prioritet = prioritet.name();
        return this;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    public OpprettOppgaveRequest setMetadata(Map<String, String> metadata) {
        Objects.requireNonNull(metadata);
        this.metadata = metadata;
        return this;
    }

    public OpprettOppgaveRequest putMetadata(String key, String value) {
        Objects.requireNonNull(key, "Key kan ikke være null");
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}
