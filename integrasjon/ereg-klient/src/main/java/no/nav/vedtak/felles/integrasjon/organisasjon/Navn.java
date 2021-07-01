package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.util.Objects;
import java.util.stream.Stream;

record Navn(String navnelinje1,
        String navnelinje2,
        String navnelinje3,
        String navnelinje4,
        String navnelinje5) {

    String getNavn() {
        return Stream.of(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .reduce("", (a, b) -> a + " " + b).trim();
    }
}