package no.nav.vedtak.felles.integrasjon.saf;

import javax.validation.constraints.NotNull;

public record HentDokumentQuery(@NotNull String journalpostId, @NotNull String dokumentId, @NotNull String variantFormat) {

    @Deprecated
    public String getJournalpostId() {
        return journalpostId();
    }

    @Deprecated
    public String getDokumentInfoId() {
        return dokumentId();
    }

    @Deprecated
    public String getVariantFormat() {
        return variantFormat();
    }
}
