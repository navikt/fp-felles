package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Feltene LocalDateTime datoDokument, String overstyrInnsynsregler skal normalt ikke settes, bruk null
public record OppdaterJournalpostRequest(String tittel, AvsenderMottaker avsenderMottaker, Bruker bruker, String tema, String behandlingstema,
                                         Sak sak, List<Tilleggsopplysning> tilleggsopplysninger, List<DokumentInfoOppdater> dokumenter,
                                         LocalDateTime datoDokument, String overstyrInnsynsregler) {


    public record DokumentInfoOppdater(String dokumentInfoId, String tittel, String brevkode) {
    }

    public static Builder ny() {
        return new Builder();
    }

    public static class Builder {
        private String tittel;
        private String tema;
        private String behandlingstema;
        private Sak sak;
        private Bruker bruker;
        private AvsenderMottaker avsenderMottaker;
        private List<Tilleggsopplysning> tilleggsopplysninger;
        private List<DokumentInfoOppdater> dokumenter;

        Builder() {
        }

        public Builder medTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder medTema(String tema) {
            this.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(String behandlingstema) {
            this.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medBruker(String aktørId) {
            this.bruker = new Bruker(aktørId, Bruker.BrukerIdType.AKTOERID);
            return this;
        }

        public Builder medAvsender(String fnr, String navn) {
            this.avsenderMottaker = new AvsenderMottaker(fnr, AvsenderMottaker.AvsenderMottakerIdType.FNR, navn);
            return this;
        }

        public Builder medSak(Sak sak) {
            this.sak = sak;
            return this;
        }

        public Builder medTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
            if (this.tilleggsopplysninger == null) {
                this.tilleggsopplysninger = new ArrayList<>();
            }
            this.tilleggsopplysninger.addAll(tilleggsopplysninger);
            return this;
        }

        public Builder leggTilTilleggsopplysning(Tilleggsopplysning tilleggsopplysning) {
            return medTilleggsopplysninger(List.of(tilleggsopplysning));
        }

        public Builder leggTilDokument(DokumentInfoOppdater dokument) {
            if (this.dokumenter == null) {
                this.dokumenter = new ArrayList<>();
            }
            this.dokumenter.add(dokument);
            return this;
        }

        public Builder leggTilDokument(String dokumentInfoId, String tittel, String brevkode) {
            if (this.dokumenter == null) {
                this.dokumenter = new ArrayList<>();
            }
            this.dokumenter.add(new DokumentInfoOppdater(dokumentInfoId, tittel, brevkode));
            return this;
        }

        public OppdaterJournalpostRequest build() {
            return new OppdaterJournalpostRequest(tittel, avsenderMottaker, bruker, tema, behandlingstema, sak, tilleggsopplysninger, dokumenter, null, null);
        }
    }

}
