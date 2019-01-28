package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import java.time.LocalDate;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.PrioritetKode;

public class OpprettOppgaveFristOgPrioritet {
    private LocalDate aktivFra;
    private LocalDate aktivTil;
    private PrioritetKode prioritetKode;

    private OpprettOppgaveFristOgPrioritet(LocalDate aktivFra, LocalDate aktivTil, PrioritetKode prioritetKode) {
        this.aktivFra = aktivFra;
        this.aktivTil = aktivTil;
        this.prioritetKode = prioritetKode;
    }

    public LocalDate getAktivFra() {
        return aktivFra;
    }

    public LocalDate getAktivTil() {
        return aktivTil;
    }

    public PrioritetKode getPrioritetKode() {
        return prioritetKode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDate aktivFra;
        private LocalDate aktivTil;
        private PrioritetKode prioritetKode;

        public Builder aktivFra(LocalDate aktivFra) {
            this.aktivFra = aktivFra;
            return this;
        }

        public Builder aktivTil(LocalDate aktivTil) {
            this.aktivTil = aktivTil;
            return this;
        }

        public Builder medPrioritet(PrioritetKode prioritetKode) {
            this.prioritetKode = prioritetKode;
            return this;
        }

        public OpprettOppgaveFristOgPrioritet build() {
            return new OpprettOppgaveFristOgPrioritet(aktivFra, aktivTil, prioritetKode);
        }
    }
}
