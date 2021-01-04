package no.nav.vedtak.felles.integrasjon.sak.v1;

import java.util.Optional;

import no.nav.vedtak.felles.integrasjon.sak.v1.SakJson.Builder;

public interface SakClient {

    SakJson hentSakId(String sakId);

    Optional<SakJson> finnForSaksnummer(String saksnummer) throws Exception;

    @Deprecated
    SakJson opprettSak(Builder request);

    SakJson opprettSak(SakJson sak);

}
