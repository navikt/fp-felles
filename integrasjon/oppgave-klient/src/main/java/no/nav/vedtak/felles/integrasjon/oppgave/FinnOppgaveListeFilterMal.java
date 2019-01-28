package no.nav.vedtak.felles.integrasjon.oppgave;

import java.util.List;

public class FinnOppgaveListeFilterMal {
    private String opprettetEnhetId;
    private String opprettetEnhetNavn;
    private String ansvarligEnhetNavn;
    private List<String> oppgavetypeKodeListe;
    private List<String> brukertypeKodeListe;

    // Har fjernet en del filter kriterier. Kan legges til senere ved behov.
    public FinnOppgaveListeFilterMal(String opprettetEnhetId, String opprettetEnhetNavn, String ansvarligEnhetNavn,
                                     List<String> oppgavetypeKodeListe, List<String> brukertypeKodeListe) {
        this.opprettetEnhetId = opprettetEnhetId;
        this.opprettetEnhetNavn = opprettetEnhetNavn;
        this.ansvarligEnhetNavn = ansvarligEnhetNavn;
        this.oppgavetypeKodeListe = oppgavetypeKodeListe;
        this.brukertypeKodeListe = brukertypeKodeListe;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getOpprettetEnhetId() {
        return opprettetEnhetId;
    }

    public String getOpprettetEnhetNavn() {
        return opprettetEnhetNavn;
    }

    public String getAnsvarligEnhetNavn() {
        return ansvarligEnhetNavn;
    }

    public List<String> getOppgavetypeKodeListe() {
        return oppgavetypeKodeListe;
    }

    public List<String> getBrukertypeKodeListe() {
        return brukertypeKodeListe;
    }

    public static class Builder {
        private String opprettetEnhetId;
        private String opprettetEnhetNavn;
        private String ansvarligEnhetNavn;
        private List<String> oppgavetypeKodeListe;
        private List<String> brukertypeKodeListe;


        public Builder medOpprettetEnhetId(String opprettetEnhetId) {
            this.opprettetEnhetId = opprettetEnhetId;
            return this;
        }

        public Builder medOpprettetEnhetNavn(String opprettetEnhetNavn) {
            this.opprettetEnhetNavn = opprettetEnhetNavn;
            return this;
        }

        public Builder medAnsvarligEnhetNavn(String ansvarligEnhetNavn) {
            this.ansvarligEnhetNavn = ansvarligEnhetNavn;
            return this;
        }

        public Builder medOppgavetypeKodeListe(List<String> oppgavetypeKodeListe) {
            this.oppgavetypeKodeListe = oppgavetypeKodeListe;
            return this;
        }

        public Builder medBrukertypeKodeListe(List<String> brukertypeKodeListe) {
            this.brukertypeKodeListe = brukertypeKodeListe;
            return this;
        }

        public FinnOppgaveListeFilterMal build() {
            return new FinnOppgaveListeFilterMal(opprettetEnhetId, opprettetEnhetNavn, ansvarligEnhetNavn, oppgavetypeKodeListe, brukertypeKodeListe);
        }
    }
}
