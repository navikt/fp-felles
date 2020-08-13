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
public class Journalpost {

    @JsonProperty("journalpostId")
    private String journalpostId;
    @JsonProperty("tittel")
    private String tittel;
    @JsonProperty("journalposttype")
    private String journalposttype;
    @JsonProperty("journalstatus")
    private String journalstatus;
    @JsonProperty("kanal")
    private String kanal;
    @JsonProperty("tema")
    private String tema;
    @JsonProperty("behandlingstema")
    private String behandlingstema;
    @JsonProperty("sak")
    private Sak sak;
    @JsonProperty("bruker")
    private Bruker bruker;
    @JsonProperty("journalforendeEnhet")
    private String journalforendeEnhet;
    @JsonProperty("dokumenter")
    private List<DokumentInfo> dokumenter;
    @JsonProperty("relevanteDatoer")
    private List<RelevantDato> relevanteDatoer;


    @JsonCreator
    public Journalpost(@JsonProperty("journalpostId") String journalpostId,
                       @JsonProperty("tittel") String tittel,
                       @JsonProperty("journalposttype") String journalposttype,
                       @JsonProperty("journalstatus") String journalstatus,
                       @JsonProperty("kanal") String kanal,
                       @JsonProperty("tema") String tema,
                       @JsonProperty("behandlingstema") String behandlingstema,
                       @JsonProperty("sak") Sak sak,
                       @JsonProperty("bruker") Bruker bruker,
                       @JsonProperty("journalforendeEnhet") String journalforendeEnhet,
                       @JsonProperty("dokumenter") List<DokumentInfo> dokumenter,
                       @JsonProperty("relevanteDatoer") List<RelevantDato> relevanteDatoer) {
        this.journalpostId = journalpostId;
        this.tittel = tittel;
        this.journalposttype = journalposttype;
        this.journalstatus = journalstatus;
        this.kanal = kanal;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.sak = sak;
        this.bruker = bruker;
        this.journalforendeEnhet = journalforendeEnhet;
        this.dokumenter = dokumenter;
        this.relevanteDatoer = relevanteDatoer;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getTittel() {
        return tittel;
    }

    public String getJournalposttype() {
        return journalposttype;
    }

    public String getJournalstatus() {
        return journalstatus;
    }

    public String getKanal() {
        return kanal;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public Sak getSak() {
        return sak;
    }

    public Bruker getBruker() {
        return bruker;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public List<DokumentInfo> getDokumenter() {
        return dokumenter;
    }

    public boolean harArkivsaksnummer() {
        if (sak == null) return false;
        return sak.getArkivsaksnummer() != null && !sak.getArkivsaksnummer().trim().isEmpty();
    }

    @Override
    public String toString() {
        return "Journalpost{" +
                "journalpostId='" + journalpostId + '\'' +
                ", tittel='" + tittel + '\'' +
                ", journalposttype='" + journalposttype + '\'' +
                ", journalstatus='" + journalstatus + '\'' +
                ", kanal='" + kanal + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", sak=" + sak +
                ", bruker=" + "<anonymisert>" +
                ", journalforendeEnhet='" + journalforendeEnhet + '\'' +
                '}';
    }

    public List<RelevantDato> getRelevanteDatoer() {
        return relevanteDatoer;
    }
}
