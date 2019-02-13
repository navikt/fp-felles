package no.nav.vedtak.sikkerhet.context;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.util.Set;

interface SubjectHandlerFeil extends DeklarerteFeil {

    SubjectHandlerFeil FACTORY = FeilFactory.create(SubjectHandlerFeil.class);

    @TekniskFeil(feilkode = "F-327190", feilmelding = "Forventet ingen eller ett element, men fikk %s elementer av type %s", logLevel = LogLevel.ERROR)
    Feil forventet0Eller1(int antall, Set<String> klasserMottatt);
}
