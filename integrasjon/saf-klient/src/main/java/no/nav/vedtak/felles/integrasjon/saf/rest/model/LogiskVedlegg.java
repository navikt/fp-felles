package no.nav.vedtak.felles.integrasjon.saf.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LogiskVedlegg {

    @JsonProperty("logiskVedleggId")
    private String logiskVedleggId;
    @JsonProperty("tittel")
    private String tittel;


    @JsonCreator
    public LogiskVedlegg(@JsonProperty("id") String logiskVedleggId,
                         @JsonProperty("tittel") String tittel) {
        this.logiskVedleggId = logiskVedleggId;
        this.tittel = tittel;
    }

    public String getLogiskVedleggId() {
        return logiskVedleggId;
    }


    public String getTittel() {
        return tittel;
    }

    @Override
    public String toString() {
        return "LogiskeVedlegg{" +
            "logiskVedleggId='" + logiskVedleggId + '\'' +
            ", tittel='" + tittel + '\'' +
            '}';
    }
}
