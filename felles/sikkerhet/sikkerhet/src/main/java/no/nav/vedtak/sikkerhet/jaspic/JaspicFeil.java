package no.nav.vedtak.sikkerhet.jaspic;

import java.security.Principal;
import java.util.Set;

import no.nav.vedtak.exception.TekniskException;

class JaspicFeil {

    private JaspicFeil() {

    }

    static TekniskException eksisterendeSubject(Set<Principal> principals, Set<String> credidentialClasses) {
        return new TekniskException("F-498054", String.format(
                "Denne SKAL rapporteres som en bug hvis den dukker opp. Tråden inneholdt allerede et Subject med følgende principals {%s} og PublicCredentials klasser {%s}. Sletter det før autentisering fortsetter.",
                principals, credidentialClasses));
    }
}
