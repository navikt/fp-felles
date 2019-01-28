package no.nav.vedtak.felles.integrasjon.oppgave;


public class FinnOppgaveListeSokMal {
    private String ansvarligEnhetId;
    private String brukerId;
    private String sakId;

    // Har fjernet en del søkefelter. Kan legges til senere ved behov.
    public FinnOppgaveListeSokMal(String ansvarligEnhetId, String brukerId, String sakId) {
        this.ansvarligEnhetId = ansvarligEnhetId;
        this.brukerId = brukerId;
        this.sakId = sakId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAnsvarligEnhetId() {
        return ansvarligEnhetId;
    }

    public String getBrukerId() {
        return brukerId;
    }

    public String getSakId() {
        return sakId;
    }

    public static class Builder {
        private String ansvarligEnhetId;
        private String brukerId;
        private String sakId;

        public Builder medAnsvarligEnhetId(String ansvarligEnhetId) {
            this.ansvarligEnhetId = ansvarligEnhetId;
            return this;
        }

        public Builder medBrukerId(String brukerId) {
            this.brukerId = brukerId;
            return this;
        }

        public Builder medSakId(String sakId) {
            this.sakId = sakId;
            return this;
        }

        public FinnOppgaveListeSokMal build() {
            return new FinnOppgaveListeSokMal(ansvarligEnhetId, brukerId, sakId);
        }
    }
}

