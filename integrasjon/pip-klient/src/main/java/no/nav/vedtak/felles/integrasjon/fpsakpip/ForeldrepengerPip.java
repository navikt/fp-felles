package no.nav.vedtak.felles.integrasjon.fpsakpip;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ForeldrepengerPip {

    List<String> personerForSak(String saksnummer);

    List<SakMedPersonerDto> personerForSaker(Set<String> saksnummer);

    String saksnummerForBehandling(UUID behandlingUuid);

    SakMedPersonerDto sakPersonerForBehandling(UUID behandlingUuid);
}
