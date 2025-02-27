package no.nav.vedtak.sikkerhet.abac;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
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
     * OIDC tokenet til brukeren. Helst fra følgende providere: TokenX, AzureAD.
     * Sendes til PDP (Policy Decision Point) og gir informasjon til ABAC om subject og auth level.
     *
     * @return bruker OIDC token.
     */
    OpenIDToken openIdToken();

    /**
     * Azure OID til brukeren. Kun satt for innkommende Azure-kall, ellers null
     *
     * @return brukers oid.
     */
    UUID getOid();

    /**
     * Gjenkjente AD-grupper til brukeren. Kun satt for innkommende Azure OBO-kall, ellers tom
     *
     * @return brukers ansattgrupper.
     */
    Set<AnsattGruppe> getAnsattGrupper();

}
