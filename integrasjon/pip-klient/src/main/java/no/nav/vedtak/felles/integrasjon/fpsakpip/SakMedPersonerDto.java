package no.nav.vedtak.felles.integrasjon.fpsakpip;

import java.util.Set;

public record SakMedPersonerDto(String saksnummer, Set<String> identer) {
}
