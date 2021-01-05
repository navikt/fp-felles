package no.nav.vedtak.felles.integrasjon.sak.v1;

import java.util.Optional;

public interface SakClient {

    SakJson hentSakId(String sakId);

    Optional<SakJson> finnForSaksnummer(String saksnummer) throws Exception;

    SakJson opprettSak(SakJson sak);

}
