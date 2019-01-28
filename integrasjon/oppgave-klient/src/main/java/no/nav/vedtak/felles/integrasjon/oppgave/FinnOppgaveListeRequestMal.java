package no.nav.vedtak.felles.integrasjon.oppgave;

import java.util.Objects;

import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeSortering;

public class FinnOppgaveListeRequestMal {
    private FinnOppgaveListeSokMal sok;
    private FinnOppgaveListeFilterMal filter;
    private FinnOppgaveListeSortering sorteringKode;
    private String ikkeTidligereFordeltTil;

    public FinnOppgaveListeRequestMal(FinnOppgaveListeSokMal sok, FinnOppgaveListeFilterMal filter,
                                      FinnOppgaveListeSortering sorteringKode, String ikkeTidligereFordeltTil) {
        this.sok = sok;
        this.filter = filter;
        this.sorteringKode = sorteringKode;
        this.ikkeTidligereFordeltTil = ikkeTidligereFordeltTil;
    }

    public FinnOppgaveListeSokMal getSok() {
        return sok;
    }

    public FinnOppgaveListeFilterMal getFilter() {
        return filter;
    }

    public FinnOppgaveListeSortering getSorteringKode() {
        return sorteringKode;
    }

    public String getIkkeTidligereFordeltTil() {
        return ikkeTidligereFordeltTil;
    }

    public static class Builder {
        private FinnOppgaveListeSokMal sok;
        private FinnOppgaveListeFilterMal filter;
        private FinnOppgaveListeSortering sorteringKode;
        private String ikkeTidligereFordeltTil;

        public Builder medSok(FinnOppgaveListeSokMal sok) {
            this.sok = sok;
            return this;
        }

        public Builder medFilter(FinnOppgaveListeFilterMal filter) {
            this.filter = filter;
            return this;
        }

        public Builder medSorteringKode(FinnOppgaveListeSortering sorteringKode) {
            this.sorteringKode = sorteringKode;
            return this;
        }

        public Builder medIkkeTidligereFordeltTil(String ikkeTidligereFordeltTil) {
            this.ikkeTidligereFordeltTil = ikkeTidligereFordeltTil;
            return this;
        }

        public FinnOppgaveListeRequestMal build() {
            Objects.requireNonNull(sok, "FinnOppgaveListeSokMal");
            return new FinnOppgaveListeRequestMal(sok, filter, sorteringKode, ikkeTidligereFordeltTil);
        }
    }
}
