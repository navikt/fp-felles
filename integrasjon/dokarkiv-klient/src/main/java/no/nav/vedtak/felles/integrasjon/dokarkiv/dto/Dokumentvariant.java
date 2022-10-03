package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.vedtak.felles.integrasjon.dokarkiv.ser.ByteArraySomBase64StringSerializer;

public record Dokumentvariant(Variantformat variantformat,
                              Filtype filtype,
                              @JsonSerialize(using = ByteArraySomBase64StringSerializer.class)
                              byte[] fysiskDokument) {

    public enum Variantformat {
        ARKIV,
        FULLVERSJON,
        PRODUKSJON,
        PRODUKSJON_DLF,
        SLADDET,
        ORIGINAL;

        public Builder medDokument(byte[] dokumentInnhold, Builder builder) {
            builder.fysiskDokument = dokumentInnhold;
            return builder;
        }
    }

    public enum Filtype {

        PDF,
        PDFA,
        XML,
        RTF,
        DLF,
        JPEG,
        TIFF,
        AXML,
        DXML,
        JSON,
        PNG;

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Variantformat variantformat;
        private Filtype filtype;
        private byte[] fysiskDokument;

        private Builder() {
        }

        public Builder medFiltype(Filtype filtype) {
            this.filtype = filtype;
            return this;
        }

        public Builder medVariantformat(Variantformat variantformat) {
            this.variantformat = variantformat;
            return this;
        }

        public Builder medDokument(byte[] dokumentInnhold) {
            this.fysiskDokument = dokumentInnhold;
            return this;
        }

        public Dokumentvariant build() {
            Objects.requireNonNull(fysiskDokument, "mangler dokumentinnhold");
            Objects.requireNonNull(filtype, "mangler filtype");
            Objects.requireNonNull(variantformat, "mangler variantformat");
            return new Dokumentvariant(variantformat, filtype, fysiskDokument);
        }
    }


}
