package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;

public class OpprettOppgaveRequest {

    private OpprettOppgave oppgave;
    private OpprettOppgaveFristOgPrioritet fristOgPrioritet;
    private OpprettOppgaveBruker bruker;
    private OpprettOppgaveDokumentOgSak dokumentOgSak;
    private boolean lest;

    private OpprettOppgaveRequest(OpprettOppgave oppgave, OpprettOppgaveFristOgPrioritet fristOgPrioritet,
                                  boolean lest, OpprettOppgaveBruker bruker, OpprettOppgaveDokumentOgSak dokumentOgSak) {
        this.oppgave = oppgave;
        this.fristOgPrioritet = fristOgPrioritet;
        this.lest = lest;
        this.bruker = bruker;
        this.dokumentOgSak = dokumentOgSak;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getNormertBehandlingsTidInnen() {
        return dokumentOgSak.getNormertBehandlingsTidInnen();
    }

    public BrukerType getBrukerTypeKode() {
        return bruker.getBrukerTypeKode();
    }

    public int getOpprettetAvEnhetId() {
        return oppgave.getOpprettetAvEnhetId();
    }

    public String getAnsvarligEnhetId() {
        return oppgave.getAnsvarligEnhetId();
    }

    public String getFnr() {
        return bruker.getFnr();
    }

    public FagomradeKode getFagomradeKode() {
        return oppgave.getFagomradeKode();
    }

    public LocalDate getAktivFra() {
        return fristOgPrioritet.getAktivFra();
    }

    public Optional<LocalDate> getAktivTil() {
        return Optional.ofNullable(fristOgPrioritet.getAktivTil());
    }

    public String getOppgavetypeKode() {
        return oppgave.getOppgavetypeKode();
    }

    public String getUnderkategoriKode() {
        return oppgave.getUnderkategoriKode();
    }

    public PrioritetKode getPrioritetKode() {
        return fristOgPrioritet.getPrioritetKode();
    }

    public String getBeskrivelse() {
        return oppgave.getBeskrivelse();
    }

    public boolean isLest() {
        return lest;
    }

    public String getSaksnummer() {
        return dokumentOgSak.getSaksnummer();
    }

    public String getDokumentId() {
        return dokumentOgSak.getDokumentId();
    }

    public LocalDate getMottattDato() {
        return dokumentOgSak.getMottattDato();
    }

    public static class Builder {

        private boolean lest;
        private OpprettOppgaveFristOgPrioritet.Builder fristOgPrioritetBuilder = OpprettOppgaveFristOgPrioritet.builder();
        private OpprettOppgave.Builder oppgaveBuilder = OpprettOppgave.builder();
        private OpprettOppgaveBruker.Builder brukerBuilder = OpprettOppgaveBruker.builder();
        private OpprettOppgaveDokumentOgSak.Builder dokumentOgSakBuilder = OpprettOppgaveDokumentOgSak.builder();

        public Builder medOpprettetAvEnhetId(int opprettetAvEnhetId) {
            this.oppgaveBuilder.opprettetAvEnhet(opprettetAvEnhetId);
            return this;
        }

        public Builder medAnsvarligEnhetId(String ansvarligEnhetId) {
            this.oppgaveBuilder.medAnsvarligEnhet(ansvarligEnhetId);
            return this;
        }

        public Builder medFagomradeKode(String fagomradeKode) {
            this.oppgaveBuilder.medFagomrade(FagomradeKode.fraString(fagomradeKode));
            return this;
        }

        public Builder medFnr(String fnr) {
            this.brukerBuilder.medFoedselsnummer(fnr);
            return this;
        }

        public Builder medAktivFra(LocalDate aktivFra) {
            this.fristOgPrioritetBuilder.aktivFra(aktivFra);
            return this;
        }

        public Builder medAktivTil(LocalDate aktivTil) {
            this.fristOgPrioritetBuilder.aktivTil(aktivTil);
            return this;
        }

        public Builder medOppgavetypeKode(String oppgavetypeKode) {
            this.oppgaveBuilder.medOppgaveType(oppgavetypeKode);
            return this;
        }

        public Builder medUnderkategoriKode(String underkategoriKode) {
            this.oppgaveBuilder.medUnderkategori(underkategoriKode);
            return this;
        }

        public Builder medSaksnummer(String saksnummmer) {
            this.dokumentOgSakBuilder.medSaksnummer(saksnummmer);
            return this;
        }

        public Builder medPrioritetKode(String prioritetKode) {
            this.fristOgPrioritetBuilder.medPrioritet(PrioritetKode.fraString(prioritetKode));
            return this;
        }

        public Builder medBeskrivelse(String beskrivelse) {
            this.oppgaveBuilder.medBeskrivelse(beskrivelse);
            return this;
        }

        public Builder medLest(boolean lest) {
            this.lest = lest;
            return this;
        }

        public Builder medDokumentId(String dokumentId) {
            this.dokumentOgSakBuilder.medDokumentId(dokumentId);
            return this;
        }

        public Builder medMottattDato(LocalDate mottattDato) {
            this.dokumentOgSakBuilder.medMottatDato(mottattDato);
            return this;
        }

        public Builder medNormertBehandlingsTidInnen(LocalDate normertBehandlingsTidInnen) {
            this.dokumentOgSakBuilder.medNormertBehandlingsTidInnen(normertBehandlingsTidInnen);
            return this;
        }

        public Builder medBrukerTypeKode(BrukerType brukerTypeKode) {
            this.brukerBuilder.medBrukerType(brukerTypeKode);
            return this;
        }

        public OpprettOppgaveRequest build() {
            return new OpprettOppgaveRequest(oppgaveBuilder.build(), fristOgPrioritetBuilder.build(), lest, brukerBuilder.build(), dokumentOgSakBuilder.build());
        }
    }
}
