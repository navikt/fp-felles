package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.Collection;

public record LdapBruker(String displayName, Collection<String> groups) {

}
