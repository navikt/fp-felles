package no.nav.vedtak.felles.integrasjon.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Variables {

    @JsonProperty("journalpostId")
    private String journalpostId;

    @JsonProperty("fagsakId")
    private String fagsakId;

    @JsonProperty("fagsaksystem")
    private String fagsaksystem;

    public Variables(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public Variables(String fagsakId, String fagsaksystem) {
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
    }

    @Override
    public String toString() {
        return "Variables{" +
            "journalpostId='" + journalpostId + '\'' +
            ", fagsakId='" + fagsakId + '\'' +
            ", fagsaksystem='" + fagsaksystem + '\'' +
            '}';
    }
}
