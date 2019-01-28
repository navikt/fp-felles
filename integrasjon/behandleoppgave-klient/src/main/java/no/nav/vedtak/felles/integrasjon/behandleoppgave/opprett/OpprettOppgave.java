package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;

public class OpprettOppgave {

    private String beskrivelse;
    private int opprettetAvEnhetId;
    private String ansvarligEnhetId;
    private FagomradeKode fagomradeKode;
    private String oppgavetypeKode;
    private String underkategoriKode;

    private OpprettOppgave(String beskrivelse, int opprettetAvEnhetId, String ansvarligEnhetId, FagomradeKode fagomradeKode, String oppgavetypeKode, String underkategoriKode) {
        this.beskrivelse = beskrivelse;
        this.opprettetAvEnhetId = opprettetAvEnhetId;
        this.ansvarligEnhetId = ansvarligEnhetId;
        this.fagomradeKode = fagomradeKode;
        this.oppgavetypeKode = oppgavetypeKode;
        this.underkategoriKode = underkategoriKode;
    }

    public int getOpprettetAvEnhetId() {
        return opprettetAvEnhetId;
    }

    public String getAnsvarligEnhetId() {
        return ansvarligEnhetId;
    }

    public FagomradeKode getFagomradeKode() {
        return fagomradeKode;
    }

    public String getOppgavetypeKode() {
        return oppgavetypeKode;
    }

    public String getUnderkategoriKode() {
        return underkategoriKode;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String beskrivelse;
        private int opprettetAvEnhetId;
        private String ansvarligEnhetId;
        private FagomradeKode fagomradeKode;
        private String oppgavetypeKode;
        private String underkategoriKode;

        public Builder medBeskrivelse(String beskrivelse) {
            this.beskrivelse = beskrivelse;
            return this;
        }

        public Builder opprettetAvEnhet(int opprettetAvEnhetId) {
            this.opprettetAvEnhetId = opprettetAvEnhetId;
            return this;
        }

        public Builder medAnsvarligEnhet(String ansvarligEnhetId) {
            this.ansvarligEnhetId = ansvarligEnhetId;
            return this;
        }

        public Builder medFagomrade(FagomradeKode fagomradeKode) {
            this.fagomradeKode = fagomradeKode;
            return this;
        }

        public Builder medOppgaveType(String oppgavetypeKode) {
            this.oppgavetypeKode = oppgavetypeKode;
            return this;
        }

        public Builder medUnderkategori(String underkategoriKode) {
            this.underkategoriKode = underkategoriKode;
            return this;
        }

        public OpprettOppgave build() {
            return new OpprettOppgave(beskrivelse, opprettetAvEnhetId, ansvarligEnhetId, fagomradeKode, oppgavetypeKode, underkategoriKode);
        }
    }

}
