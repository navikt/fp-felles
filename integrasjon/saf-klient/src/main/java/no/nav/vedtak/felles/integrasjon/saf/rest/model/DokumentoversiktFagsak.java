package no.nav.vedtak.felles.integrasjon.saf.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DokumentoversiktFagsak {

    @JsonProperty("journalposter")
    private List<Journalpost> journalposter;

    @JsonCreator
    public DokumentoversiktFagsak(@JsonProperty("journalposter") List<Journalpost> journalposter) {
        this.journalposter = journalposter;
    }

    public List<Journalpost> getJournalposter() {
        return journalposter;
    }

    @Override
    public String toString() {
        return "DokumentoversiktFagsak{" +
            "journalposter=" + journalposter +
            '}';
    }
}
