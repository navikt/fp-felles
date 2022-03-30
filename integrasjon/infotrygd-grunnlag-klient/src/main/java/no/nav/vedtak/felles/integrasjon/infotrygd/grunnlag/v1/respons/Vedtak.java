package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Vedtak(Periode periode, int utbetalingsgrad, String arbeidsgiverOrgnr, Boolean erRefusjon, Integer dagsats) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Periode getPeriode() {
        return periode();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public int getUtbetalingsgrad() {
        return utbetalingsgrad();
    }
}
