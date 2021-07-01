package no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons;

import java.time.LocalDate;


public record Periode(LocalDate fom, LocalDate tom) {

    @Deprecated
    public LocalDate getFom() {
        return fom();
    }

    @Deprecated
    public LocalDate getTom() {
        return tom();
    }



}
