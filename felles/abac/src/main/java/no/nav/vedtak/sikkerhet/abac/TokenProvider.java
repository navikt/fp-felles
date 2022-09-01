package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

/**
 * Interface brukes til å provide informasjon om brukeren som forespør tilgang til en ressurs i en applikasjon.
 */
public interface TokenProvider {

    /**
     * Identifikator til brukeren. Brukes i auditlogging.
     * @return bruker id.
     */
    String getUid();

    /**
     * OIDC tokenet til brukeren. Helst fra følgende providere: Tokendings, AzureAD, STS, OpenAM.
     * Sendes til PDP (Policy Decision Point) og gir informasjon til ABAC om subject og auth level.
     * @return bruker OIDC token.
     */
    @Deprecated(forRemoval = true)
    String userToken();

    /**
     * OIDC tokenet til brukeren. Helst fra følgende providere: Tokendings, AzureAD, STS, OpenAM.
     * Sendes til PDP (Policy Decision Point) og gir informasjon til ABAC om subject og auth level.
     * @return bruker OIDC token.
     */
    OpenIDToken openIdToken();

    /**
     * SAML tokenet til brukeren. Trenges ved kall fra WebService grensesnitt.
     * Sendes til PDP (Policy Decision Point) og gir informasjon til ABAC om subject og auth level.
     * @return bruker SAML token.
     */
    String samlToken();
}
