package no.nav.vedtak.felles.integrasjon.medl2;

public record Studieinformasjon(String studieland) {

    @Deprecated
    String getStudieland() {
        return studieland();
    }
}