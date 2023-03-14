package no.nav.vedtak.felles.integrasjon.dokarkiv.dto;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import no.nav.vedtak.felles.integrasjon.dokarkiv.ser.ByteArraySomBase64StringSerializer;

public record Dokumentvariant(Variantformat variantformat, Filtype filtype,
                              @JsonSerialize(using = ByteArraySomBase64StringSerializer.class) byte[] fysiskDokument) {

    public enum Variantformat {
        ARKIV,
        FULLVERSJON,
        PRODUKSJON,
        PRODUKSJON_DLF,
        SLADDET,
        ORIGINAL;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Dokumentvariant that && variantformat == that.variantformat && filtype == that.filtype && Arrays.equals(fysiskDokument,
            that.fysiskDokument);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(variantformat, filtype);
        result = 31 * result + Arrays.hashCode(fysiskDokument);
        return result;
    }

    @Override
    public String toString() {
        return "Dokumentvariant{" + "variantformat=" + variantformat + ", filtype=" + filtype + '}';
    }
}
