package no.nav.vedtak.felles.integrasjon.saf.graphql;

import java.util.List;

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

    @JsonProperty("tilknyttedeJournalposter")
    private List<Journalpost> tilknyttedeJournalposter;

    @JsonCreator
    public GrapQlData(@JsonProperty("journalpost") Journalpost journalpost,
                      @JsonProperty("dokumentoversiktFagsakQuery") DokumentoversiktFagsak dokumentoversiktFagsak,
                      @JsonProperty("tilknyttedeJournalposter") List<Journalpost> tilknyttedeJournalposter) {
        this.journalpost = journalpost;
        this.dokumentoversiktFagsak = dokumentoversiktFagsak;
        this.tilknyttedeJournalposter = tilknyttedeJournalposter;
    }

    public Journalpost getJournalpost() {
        return journalpost;
    }

    public DokumentoversiktFagsak getDokumentoversiktFagsak() {
        return dokumentoversiktFagsak;
    }

    public List<Journalpost> getTilknyttedeJournalposter() {
        return tilknyttedeJournalposter;
    }

    @Override
    public String toString() {
        return "GrapQlData{" +
            "journalpost=" + journalpost +
            ", dokumentoversiktFagsak=" + dokumentoversiktFagsak +
            ", tilknyttedeJournalposter=" + tilknyttedeJournalposter +
            '}';
    }
}
