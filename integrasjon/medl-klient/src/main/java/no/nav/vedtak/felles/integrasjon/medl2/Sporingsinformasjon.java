package no.nav.vedtak.felles.integrasjon.medl2;

import java.time.LocalDate;

public record Sporingsinformasjon(LocalDate besluttet, String kilde) {

    @Deprecated
    LocalDate getBesluttet() {
        return besluttet();
    }

    @Deprecated
    String getKilde() {
        return kilde();
    }
}