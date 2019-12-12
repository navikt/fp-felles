package no.nav.vedtak.felles.integrasjon.aktør.klient;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

interface AktørConsumerFeil extends DeklarerteFeil {

    @IntegrasjonFeil(feilkode = "F-502945", feilmelding = "Det finnes flere aktører med samme ident", logLevel = LogLevel.WARN, exceptionClass = DetFinnesFlereAktørerMedSammePersonIdentException.class)
    Feil flereAktørerMedSammeIdent();

    @IntegrasjonFeil(feilkode = "F-408345", feilmelding = "Mottok feilmelding fra aktør-tjenesten: %s", logLevel = LogLevel.WARN)
    Feil feilmeldingFraAktørtjenesten(String feilmelding);
}
