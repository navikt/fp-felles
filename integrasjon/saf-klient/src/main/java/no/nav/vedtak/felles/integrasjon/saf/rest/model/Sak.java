package no.nav.vedtak.felles.integrasjon.saf.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Sak {

    @JsonProperty("arkivsaksystem")
    private String arkivsaksystem;
    @JsonProperty("arkivsaksnummer")
    private String arkivsaksnummer;
    @JsonProperty("fagsaksystem")
    private String fagsaksystem;
    @JsonProperty("fagsakId")
    private String fagsakId;
    @JsonProperty("sakstype")
    private Sakstype sakstype;

    @JsonCreator
    public Sak(@JsonProperty("arkivsaksystem") String arkivsaksystem, @JsonProperty("arkivsaksnummer") String arkivsaksnummer, @JsonProperty("fagsaksystem") String fagsaksystem, @JsonProperty("fagsakId") String fagsakId, @JsonProperty("sakstype") Sakstype sakstype) {
        this.arkivsaksystem = arkivsaksystem;
        this.arkivsaksnummer = arkivsaksnummer;
        this.fagsaksystem = fagsaksystem;
        this.fagsakId = fagsakId;
        this.sakstype = sakstype;
    }

    public String getFagsaksystem() {
        return fagsaksystem;
    }

    public String getFagsakId() {
        return fagsakId;
    }

    public Sakstype getSakstype() {
        return sakstype;
    }

    public String getArkivsaksystem() {
        return arkivsaksystem;
    }

    public String getArkivsaksnummer() {
        return arkivsaksnummer;
    }

    @Override
    public String toString() {
        return "Sak{" +
                "fagsaksystem='" + fagsaksystem + '\'' +
                ", fagsakId='" + fagsakId + '\'' +
                ", sakstype=" + sakstype +
                '}';
    }
}
