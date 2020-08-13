package no.nav.vedtak.felles.integrasjon.saf.rest.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public
class RelevantDato {

    @JsonProperty("dato")
    private LocalDateTime dato;

    @JsonProperty("datotype")
    private Datotype datotype;

    @JsonCreator
    public RelevantDato(@JsonProperty("journalpostId") LocalDateTime dato,
                        @JsonProperty("tittel") Datotype datotype) {
        this.dato = dato;
        this.datotype = datotype;
    }

    public LocalDateTime getDato() {
        return dato;
    }

    public Datotype getDatotype() {
        return datotype;
    }

    @Override
    public String toString() {
        return "RelevantDato{" +
            "dato=" + dato +
            ", datotype=" + datotype +
            '}';
    }
}
