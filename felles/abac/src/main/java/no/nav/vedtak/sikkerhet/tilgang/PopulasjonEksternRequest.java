package no.nav.vedtak.sikkerhet.tilgang;

import java.util.Set;

public record PopulasjonEksternRequest(String subjectPersonIdent,
                                       Set<String> personIdenter,
                                       Set<String> aktørIdenter) {

}
