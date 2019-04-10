package no.nav.vedtak.sikkerhet.jaspic;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.security.Principal;
import java.util.Set;

public interface JaspicFeil extends DeklarerteFeil {
    JaspicFeil FACTORY = FeilFactory.create(JaspicFeil.class);

    @TekniskFeil(feilkode = "F-498054", feilmelding = "Denne SKAL rapporteres som en bug hvis den dukker opp. Tråden inneholdt allerede et Subject med følgende principals {%s} og PublicCredentials klasser {%s}. Sletter det før autentisering fortsetter.", logLevel = LogLevel.INFO)
    Feil eksisterendeSubject(Set<Principal> principals, Set<String> credidentialClasses);
}
