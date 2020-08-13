package no.nav.vedtak.felles.integrasjon.saf.graphql;

import javax.validation.constraints.NotNull;

public class HentDokumentQuery implements SafQuery {

    @NotNull
    private final String journalpostId;

    @NotNull
    private final String dokumentId;

    @NotNull
    private final String variantFormat;

    public HentDokumentQuery(@NotNull String journalpostId, @NotNull String dokumentId, @NotNull String variantFormat) {
        this.journalpostId = journalpostId;
        this.dokumentId = dokumentId;
        this.variantFormat = variantFormat;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentInfoId() {
        return dokumentId;
    }

    public String getVariantFormat() {
        return variantFormat;
    }

    @Override
    public String toString() {
        return "HentDokumentQuery{" +
            "journalpostId='" + journalpostId + '\'' +
            ", dokumentId='" + dokumentId + '\'' +
            ", variantFormat='" + variantFormat + '\'' +
            '}';
    }
}
