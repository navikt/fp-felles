package no.nav.vedtak.felles.integrasjon.behandleoppgave;

public class FerdigstillOppgaveRequestMal {
    private String oppgaveId;
    private int ferdigstiltAvEnhetId;

    private FerdigstillOppgaveRequestMal(String oppgaveId, int ferdigstiltAvEnhetId) {
        this.oppgaveId = oppgaveId;
        this.ferdigstiltAvEnhetId = ferdigstiltAvEnhetId;
    }

    public String getOppgaveId() {
        return oppgaveId;
    }

    public int getFerdigstiltAvEnhetId() {
        return ferdigstiltAvEnhetId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String oppgaveId;
        private int ferdigstiltAvEnhetId;

        public FerdigstillOppgaveRequestMal.Builder medOppgaveId(String oppgaveId) {
            this.oppgaveId = oppgaveId;
            return this;
        }

        public FerdigstillOppgaveRequestMal.Builder medFerdigstiltAvEnhetId(int ferdigstiltAvEnhetId) {
            this.ferdigstiltAvEnhetId = ferdigstiltAvEnhetId;
            return this;
        }

        public FerdigstillOppgaveRequestMal build() {
            return new FerdigstillOppgaveRequestMal(oppgaveId, ferdigstiltAvEnhetId);
        }
    }
}
