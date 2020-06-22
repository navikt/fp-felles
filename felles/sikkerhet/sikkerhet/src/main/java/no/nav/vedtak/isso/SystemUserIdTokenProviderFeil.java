package no.nav.vedtak.isso;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.sikkerhet.oidc.Fikk40xKodeException;
import no.nav.vedtak.sikkerhet.oidc.VlIOException;

interface SystemUserIdTokenProviderFeil extends DeklarerteFeil {

    SystemUserIdTokenProviderFeil FACTORY = FeilFactory.create(SystemUserIdTokenProviderFeil.class);

    @IntegrasjonFeil(feilkode = "F-116509", feilmelding = "Klarte ikke hente ID-token for systembrukeren", logLevel = LogLevel.ERROR)
    Feil klarteIkkeHenteIdTokenIOException(IOException e);

    @IntegrasjonFeil(feilkode = "F-572075", feilmelding = "Klarte ikke hente ID-token for systembrukeren", logLevel = LogLevel.ERROR)
    Feil klarteIkkeHenteIdTokenVlIOException(VlIOException e);

    @IntegrasjonFeil(feilkode = "F-061582", feilmelding = "Klarte ikke hente ID-token for systembrukeren, selv etter %s forsøk", logLevel = LogLevel.ERROR)
    Feil klarteIkkeHenteIdToken(int antall, Fikk40xKodeException e);

}
