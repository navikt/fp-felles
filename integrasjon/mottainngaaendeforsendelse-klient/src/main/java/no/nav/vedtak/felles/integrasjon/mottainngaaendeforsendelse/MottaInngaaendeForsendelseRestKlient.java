package no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRequest;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;

public interface MottaInngaaendeForsendelseRestKlient {
    MottaInngaaendeForsendelseResponse journalførForsendelse(MottaInngaaendeForsendelseRequest request);
}
