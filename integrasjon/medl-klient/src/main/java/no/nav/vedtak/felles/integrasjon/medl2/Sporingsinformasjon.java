package no.nav.vedtak.felles.integrasjon.medl2;

import java.time.LocalDate;

public record Sporingsinformasjon(LocalDate besluttet, String kilde) {

    @Deprecated(since = "4.0.x", forRemoval = true)
    LocalDate getBesluttet() {
        return besluttet();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    String getKilde() {
        return kilde();
    }
}