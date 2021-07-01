package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

public record Vedtak(Periode periode, int utbetalingsgrad) {

    @Deprecated
    public Periode getPeriode() {
        return periode();
    }

    @Deprecated
    public int getUtbetalingsgrad() {
        return utbetalingsgrad();
    }
}
