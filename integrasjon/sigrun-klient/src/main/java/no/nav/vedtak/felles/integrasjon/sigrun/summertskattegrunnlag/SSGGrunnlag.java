package no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag;

public class SSGGrunnlag {
    private String tekniskNavn;
    private String beloep;

    public String getTekniskNavn() {
        return this.tekniskNavn;
    }

    public String getBeloep() {
        return this.beloep;
    }

    public void setTekniskNavn(String tekniskNavn) {
        this.tekniskNavn = tekniskNavn;
    }

    public void setBeloep(String beloep) {
        this.beloep = beloep;
    }

    public SSGGrunnlag() {
    }

    public SSGGrunnlag(String tekniskNavn, String beloep) {
        this.tekniskNavn = tekniskNavn;
        this.beloep = beloep;
    }
}
