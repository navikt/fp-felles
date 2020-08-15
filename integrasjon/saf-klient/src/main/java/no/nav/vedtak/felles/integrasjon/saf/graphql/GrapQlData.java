package no.nav.vedtak.felles.integrasjon.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.felles.integrasjon.saf.rest.model.DokumentoversiktFagsak;
import no.nav.vedtak.felles.integrasjon.saf.rest.model.Journalpost;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GrapQlData {

    @JsonProperty("journalpost")
    private Journalpost journalpost;

    @JsonProperty("dokumentoversiktFagsak")
    private DokumentoversiktFagsak dokumentoversiktFagsak;

    @JsonCreator
    public GrapQlData(@JsonProperty("journalpost") Journalpost journalpost,
                      @JsonProperty("dokumentoversiktFagsakQuery") DokumentoversiktFagsak dokumentoversiktFagsak) {
        this.journalpost = journalpost;
        this.dokumentoversiktFagsak = dokumentoversiktFagsak;
    }


    public Journalpost getJournalpost() {
        return journalpost;
    }

    public DokumentoversiktFagsak getDokumentoversiktFagsak() {
        return dokumentoversiktFagsak;
    }

    @Override
    public String toString() {
        return "GrapQlData{" +
            "journalpost=" + journalpost +
            ", dokumentoversiktFagsak=" + dokumentoversiktFagsak +
            '}';
    }
}
