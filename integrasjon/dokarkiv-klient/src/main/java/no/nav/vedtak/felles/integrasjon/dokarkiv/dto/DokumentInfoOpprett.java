package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.ArrayList;
import java.util.List;

// Rekkefølge er normalt ikke i bruk, men null for hoveddokument og 1,2,... for vedlegg. Se swagger for semantikk
public record DokumentInfoOpprett(String tittel, String brevkode, Integer rekkefoelge, List<Dokumentvariant> dokumentvarianter) {


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String tittel;
        private String brevkode;
        private Integer rekkefoelge;
        private final List<Dokumentvariant> dokumentvarianter;

        private Builder() {
            this.dokumentvarianter = new ArrayList<>();
        }

        public Builder medTittel(String tittel) {
            this.tittel = tittel;
            return this;
        }

        public Builder medBrevkode(String brevkode) {
            this.brevkode = brevkode;
            return this;
        }

        // Se swagger for semantikk. Bruk null for hoveddokument
        public Builder medRekkefølge(Integer rekkefoelge) {
            this.rekkefoelge = rekkefoelge;
            return this;
        }

        public Builder leggTilDokumentvariant(Dokumentvariant dokumentvarianter) {
            this.dokumentvarianter.add(dokumentvarianter);
            return this;
        }

        public DokumentInfoOpprett build() {
            if (this.dokumentvarianter.isEmpty()) {
                throw new IllegalArgumentException("Krever minst 1 dokumentvariant");
            }
            if (this.dokumentvarianter.stream().noneMatch(d -> Dokumentvariant.Variantformat.ARKIV.equals(d.variantformat()))) {
                throw new IllegalArgumentException("Krever at det finnes variant av type " + Dokumentvariant.Variantformat.ARKIV);
            }
            return new DokumentInfoOpprett(tittel, brevkode, rekkefoelge, dokumentvarianter);
        }
    }
}
