package no.nav.vedtak.felles.integrasjon.medl2;

public record Studieinformasjon(String studieland) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    String getStudieland() {
        return studieland();
    }
}