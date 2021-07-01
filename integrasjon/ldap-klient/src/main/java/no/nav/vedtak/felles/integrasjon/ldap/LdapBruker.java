package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.Collection;

public record LdapBruker(String displayName, Collection<String> groups) {


    @Deprecated
    public String getDisplayName() {
        return displayName();
    }

    @Deprecated
    public Collection<String> getGroups() {
        return groups();
    }
}
