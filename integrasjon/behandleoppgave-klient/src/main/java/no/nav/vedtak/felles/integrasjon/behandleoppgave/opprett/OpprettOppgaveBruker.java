package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.BrukerType;

public class OpprettOppgaveBruker {
    private String fnr;
    private BrukerType brukerTypeKode;

    private OpprettOppgaveBruker(String fnr, BrukerType brukerTypeKode) {
        this.fnr = fnr;
        this.brukerTypeKode = brukerTypeKode;
    }

    public String getFnr() {
        return fnr;
    }

    public BrukerType getBrukerTypeKode() {
        return brukerTypeKode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fnr;
        private BrukerType brukerTypeKode;

        public Builder medFoedselsnummer(String fnr) {
            this.fnr = fnr;
            return this;
        }

        public Builder medBrukerType(BrukerType brukerType) {
            this.brukerTypeKode = brukerType;
            return this;
        }

        public OpprettOppgaveBruker build() {
            return new OpprettOppgaveBruker(fnr, brukerTypeKode);
        }
    }
}
