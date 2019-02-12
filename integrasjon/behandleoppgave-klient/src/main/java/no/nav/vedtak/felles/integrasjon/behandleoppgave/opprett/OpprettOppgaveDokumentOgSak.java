package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import java.time.LocalDate;

public class OpprettOppgaveDokumentOgSak {

    private String saksnummer;
    private String dokumentId;
    private LocalDate mottattDato;
    private LocalDate normertBehandlingsTidInnen;

    private OpprettOppgaveDokumentOgSak(String saksnummer, String dokumentId, LocalDate mottattDato, LocalDate normertBehandlingsTidInnen) {
        this.saksnummer = saksnummer;
        this.dokumentId = dokumentId;
        this.mottattDato = mottattDato;
        this.normertBehandlingsTidInnen = normertBehandlingsTidInnen;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public LocalDate getNormertBehandlingsTidInnen() {
        return normertBehandlingsTidInnen;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String saksnummer;
        private String dokumentId;
        private LocalDate mottattDato;
        private LocalDate normertBehandlingsTidInnen;

        public Builder medSaksnummer(String saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        public Builder medDokumentId(String dokumentId) {
            this.dokumentId = dokumentId;
            return this;
        }

        public Builder medMottatDato(LocalDate mottattDato) {
            this.mottattDato = mottattDato;
            return this;
        }

        public Builder medNormertBehandlingsTidInnen(LocalDate normertBehandlingsTidInnen) {
            this.normertBehandlingsTidInnen = normertBehandlingsTidInnen;
            return this;
        }

        public OpprettOppgaveDokumentOgSak build() {
            return new OpprettOppgaveDokumentOgSak(saksnummer, dokumentId, mottattDato, normertBehandlingsTidInnen);
        }
    }
}
