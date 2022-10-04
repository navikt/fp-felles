package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record OpprettJournalpostRequest(JournalpostType journalpostType,
                                        String tittel,
                                        AvsenderMottaker avsenderMottaker, String kanal,
                                        Bruker bruker, String tema,
                                        String behandlingstema,
                                        Sak sak, String journalfoerendeEnhet,
                                        LocalDate datoMottatt,
                                        String eksternReferanseId,
                                        List<Tilleggsopplysning> tilleggsopplysninger,
                                        List<DokumentInfoOpprett> dokumenter) {

    public static OpprettJournalpostRequestBuilder nyInngående() {
        return new OpprettJournalpostRequestBuilder(JournalpostType.INNGAAENDE);
    }

    public static OpprettJournalpostRequestBuilder nyUtgående() {
        return new OpprettJournalpostRequestBuilder(JournalpostType.UTGAAENDE);
    }

    public static class OpprettJournalpostRequestBuilder {
        private JournalpostType journalpostType;
        private String tittel;
        private String kanal;
        private String tema;
        private String behandlingstema;
        private String journalfoerendeEnhet;
        private LocalDate datoMottatt;
        private String eksternReferanseId;
        private Bruker bruker;
        private AvsenderMottaker avsenderMottaker;
        private Sak sak;
        private List<Tilleggsopplysning> tilleggsopplysninger;
        private List<DokumentInfoOpprett> dokumenter;

        private OpprettJournalpostRequestBuilder(JournalpostType journalpostType) {
            this.journalpostType = journalpostType;
            this.dokumenter = new ArrayList<>();
        }

        public OpprettJournalpostRequestBuilder medTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public OpprettJournalpostRequestBuilder medKanal(String kanal) {
            this.kanal = kanal;
            return this;
        }

        public OpprettJournalpostRequestBuilder medTema(String tema) {
            this.tema = tema;
            return this;
        }

        public OpprettJournalpostRequestBuilder medBehandlingstema(String behandlingstema) {
            this.behandlingstema = behandlingstema;
            return this;
        }

        public OpprettJournalpostRequestBuilder medJournalfoerendeEnhet(String journalfoerendeEnhet) {
            this.journalfoerendeEnhet = journalfoerendeEnhet;
            return this;
        }

        public OpprettJournalpostRequestBuilder medDatoMottatt(LocalDate datoMottatt) {
            this.datoMottatt = datoMottatt;
            return this;
        }

        public OpprettJournalpostRequestBuilder medEksternReferanseId(String eksternReferanseId) {
            this.eksternReferanseId = eksternReferanseId;
            return this;
        }

        public OpprettJournalpostRequestBuilder medBruker(Bruker bruker) {
            this.bruker = bruker;
            return this;
        }

        public OpprettJournalpostRequestBuilder medAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
            this.avsenderMottaker = avsenderMottaker;
            return this;
        }

        public OpprettJournalpostRequestBuilder medSak(Sak sak) {
            this.sak = sak;
            return this;
        }

        public OpprettJournalpostRequestBuilder medTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
            this.tilleggsopplysninger = tilleggsopplysninger;
            return this;
        }

        public OpprettJournalpostRequestBuilder medDokumenter(List<DokumentInfoOpprett> dokumenter) {
            this.dokumenter.addAll(dokumenter);
            return this;
        }

        public OpprettJournalpostRequestBuilder leggTilDokument(DokumentInfoOpprett.Builder dokument) {
            this.dokumenter.add(dokument.build());
            return this;
        }

        public OpprettJournalpostRequest build() {
            if (dokumenter.isEmpty()) {
                throw new IllegalArgumentException("Mangler dokumenter tiul ny journalpost");
            }
            return new OpprettJournalpostRequest(journalpostType, tittel, avsenderMottaker, kanal, bruker,
                tema, behandlingstema, sak, journalfoerendeEnhet, datoMottatt, eksternReferanseId,
                tilleggsopplysninger, dokumenter);
        }
    }
}
