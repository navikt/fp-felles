package no.nav.vedtak.felles.integrasjon.arbeidsfordeling;

public record ArbeidsfordelingRequest(String temagruppe,
                                      String tema,
                                      String behandlingstema,
                                      String behandlingstype,
                                      String diskresjonskode,
                                      String geografiskOmraade,
                                      String oppgavetype) {


    public static Builder ny() {
        return new Builder();
    }


    public static class Builder {
        private String behandlingstema;
        private String behandlingstype;
        private String diskresjonskode;
        private String geografiskOmraade;
        private String oppgavetype;
        private String tema;
        private String temagruppe;

        private Builder() {
        }

        public Builder medBehandlingstema(String behandlingstema) {
            this.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medBehandlingstype(String behandlingstype) {
            this.behandlingstype = behandlingstype;
            return this;
        }

        public Builder medDiskresjonskode(String diskresjonskode) {
            this.diskresjonskode = diskresjonskode;
            return this;
        }

        public Builder medGeografiskOmraade(String geografiskOmraade) {
            this.geografiskOmraade = geografiskOmraade;
            return this;
        }

        public Builder medOppgavetype(String oppgavetype) {
            this.oppgavetype = oppgavetype;
            return this;
        }

        public Builder medTema(String tema) {
            this.tema = tema;
            return this;
        }

        public Builder medTemagruppe(String temagruppe) {
            this.temagruppe = temagruppe;
            return this;
        }

        public ArbeidsfordelingRequest build() {
            return new ArbeidsfordelingRequest(temagruppe, tema, behandlingstema, behandlingstype,
                diskresjonskode, geografiskOmraade, oppgavetype);
        }
    }
}
