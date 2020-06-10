package no.nav.vedtak.felles.integrasjon.organisasjon;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AdresseEReg {

    @JsonProperty("adresselinje1")
    private String adresselinje1;
    @JsonProperty("adresselinje2")
    private String adresselinje2;
    @JsonProperty("adresselinje3")
    private String adresselinje3;
    @JsonProperty("kommunenummer")
    private String kommunenummer;
    @JsonProperty("landkode")
    private String landkode;
    @JsonProperty("postnummer")
    private String postnummer;
    @JsonProperty("poststed")
    private String poststed;


    public String getAdresselinje1() {
        return adresselinje1;
    }

    public String getAdresselinje2() {
        return adresselinje2;
    }

    public String getAdresselinje3() {
        return adresselinje3;
    }

    public String getKommunenummer() {
        return kommunenummer;
    }

    public String getLandkode() {
        return landkode;
    }

    public String getPostnummer() {
        return postnummer;
    }

    public String getPoststed() {
        return poststed;
    }

    @Override
    public String toString() {
        return "Forretningsadresse{" +
                "adresselinje1='" + adresselinje1 + '\'' +
                '}';
    }
}

