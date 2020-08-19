package no.nav.vedtak.felles.integrasjon.saf.graphql;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TilknyttedeJournalposterQuery implements SafQuery {

    @NotNull
    private String dokumentInfoId;

    public TilknyttedeJournalposterQuery(@NotNull String dokumentInfoId) {
        this.dokumentInfoId = dokumentInfoId;
    }

    public String getDokumentInfoId() {
        return dokumentInfoId;
    }

    @Override
    public String toString() {
        return "TilknyttedeJournalposterQuery{" +
            "dokumentInfoId='" + dokumentInfoId + '\'' +
            '}';
    }

}
