package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.time.LocalDate;


public record Periode(LocalDate fom, LocalDate tom) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    public LocalDate getFom() {
        return fom();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public LocalDate getTom() {
        return tom();
    }



}
