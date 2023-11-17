package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

/**
 * Interface brukes til å provide informasjon om brukeren som forespør tilgang til en ressurs i en applikasjon.
 */
public interface TokenProvider {

    /**
     * Identifikator til brukeren. Brukes i auditlogging.
     *
     * @return bruker id.
     */
    String getUid();

    /**
     * Kategori bruker utledet i tokenvalidering
     */
    IdentType getIdentType();

    /**
     * OIDC tokenet til brukeren. Helst fra følgende providere: TokenX, AzureAD, STS.
     * Sendes til PDP (Policy Decision Point) og gir informasjon til ABAC om subject og auth level.
     *
     * @return bruker OIDC token.
     */
    OpenIDToken openIdToken();

}
