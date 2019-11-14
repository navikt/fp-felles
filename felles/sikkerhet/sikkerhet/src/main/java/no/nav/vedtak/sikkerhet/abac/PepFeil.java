package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;

interface PepFeil extends DeklarerteFeil {
    PepFeil FACTORY = FeilFactory.create(PepFeil.class);

    @ManglerTilgangFeil(feilkode = "F-788257", feilmelding = "Tilgangskontroll.Avslag.EgenAnsatt", logLevel = LogLevel.INFO, exceptionClass = PepNektetTilgangException.class)
    Feil ikkeTilgangEgenAnsatt();

    @ManglerTilgangFeil(feilkode = "F-027901", feilmelding = "Tilgangskontroll.Avslag.Kode7", logLevel = LogLevel.INFO, exceptionClass = PepNektetTilgangException.class)
    Feil ikkeTilgangKode7();

    @ManglerTilgangFeil(feilkode = "F-709170", feilmelding = "Tilgangskontroll.Avslag.Kode6", logLevel = LogLevel.INFO, exceptionClass = PepNektetTilgangException.class)
    Feil ikkeTilgangKode6();

    @ManglerTilgangFeil(feilkode = "F-608625", feilmelding = "Ikke tilgang", logLevel = LogLevel.INFO, exceptionClass = PepNektetTilgangException.class)
    Feil ikkeTilgang();
}
