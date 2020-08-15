package no.nav.vedtak.felles.integrasjon.saf.graphql;

import javax.validation.constraints.NotNull;

public class JournalpostQuery implements SafQuery {

    @NotNull
    private String journalpostId;

    public JournalpostQuery(@NotNull String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    @Override
    public String toString() {
        return "JournalpostQuery{" +
            "journalpostId='" + journalpostId + '\'' +
            '}';
    }
}
