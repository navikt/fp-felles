package no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag;

import java.util.List;

public class SSGResponse {
    private List<SSGGrunnlag> grunnlag;
    private List<SSGGrunnlag> svalbardGrunnlag;
    private String skatteoppgjoersdato;

    public SSGResponse() {
    }

    public SSGResponse(List<SSGGrunnlag> grunnlag, List<SSGGrunnlag> svalbardGrunnlag, String skatteoppgjoersdato) {
        this.grunnlag = grunnlag;
        this.svalbardGrunnlag = svalbardGrunnlag;
        this.skatteoppgjoersdato = skatteoppgjoersdato;
    }

    public List<SSGGrunnlag> getGrunnlag() {
        return this.grunnlag;
    }

    public void setGrunnlag(List<SSGGrunnlag> grunnlag) {
        this.grunnlag = grunnlag;
    }

    public List<SSGGrunnlag> getSvalbardGrunnlag() {
        return this.svalbardGrunnlag;
    }

    public void setSvalbardGrunnlag(List<SSGGrunnlag> svalbardGrunnlag) {
        this.svalbardGrunnlag = svalbardGrunnlag;
    }

    public String getSkatteoppgjoersdato() {
        return this.skatteoppgjoersdato;
    }

    public void setSkatteoppgjoersdato(String skatteoppgjoersdato) {
        this.skatteoppgjoersdato = skatteoppgjoersdato;
    }

}
