package no.nav.vedtak.felles.integrasjon.aktør.klient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;

public interface AktørConsumer {
    Optional<String> hentAktørIdForPersonIdent(String personIdent);

    Optional<String> hentPersonIdentForAktørId(String aktørId);

    List<AktoerIder> hentAktørIdForPersonIdentSet(Set<String> requestSet);
}
