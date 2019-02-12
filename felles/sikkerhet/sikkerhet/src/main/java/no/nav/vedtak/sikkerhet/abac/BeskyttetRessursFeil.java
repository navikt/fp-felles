package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import javax.xml.transform.TransformerException;

import static no.nav.vedtak.feil.LogLevel.ERROR;

interface BeskyttetRessursFeil extends DeklarerteFeil {

    BeskyttetRessursFeil FACTORY = FeilFactory.create(BeskyttetRessursFeil.class);

    @TekniskFeil(feilkode = "F-340074", feilmelding = "Kunne ikke gjøre SAML token om til streng ", logLevel = ERROR)
    Feil kunneIkkeGjøreSamlTokenOmTilStreng(TransformerException e);

    @TekniskFeil(feilkode = "F-261962", feilmelding = "Ugyldig input forventet at samling inneholdt bare AbacDto-er, men fant %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputForventetAbacDto(String klassenavn);
}
