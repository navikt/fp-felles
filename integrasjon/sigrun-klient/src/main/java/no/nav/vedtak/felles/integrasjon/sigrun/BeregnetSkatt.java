package no.nav.vedtak.felles.integrasjon.sigrun;

public class BeregnetSkatt {

    private String tekniskNavn;
    private String verdi;

    public BeregnetSkatt() {
    }

    public BeregnetSkatt(String tekniskNavn, String verdi) {
        this.tekniskNavn = tekniskNavn;
        this.verdi = verdi;
    }

    public String getTekniskNavn() {
        return tekniskNavn;
    }

    public void setTekniskNavn(String value) {
        this.tekniskNavn = value;
    }

    public String getVerdi() {
        return verdi;
    }

    public void setVerdi(String value) {
        this.verdi = value;
    }

}
