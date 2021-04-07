package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

class Navn {

    @JsonProperty("navnelinje1")
    private String navnelinje1;
    @JsonProperty("navnelinje2")
    private String navnelinje2;
    @JsonProperty("navnelinje3")
    private String navnelinje3;
    @JsonProperty("navnelinje4")
    private String navnelinje4;
    @JsonProperty("navnelinje5")
    private String navnelinje5;

    private Navn() {
    }

    String getNavn() {
        return Stream.of(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(n -> !n.isEmpty())
                .reduce("", (a, b) -> a + " " + b).trim();
    }
}