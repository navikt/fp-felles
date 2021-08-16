package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.Collection;

public record LdapBruker(String displayName, Collection<String> groups) {


    @Deprecated(since = "4.0.x", forRemoval = true)
    public String getDisplayName() {
        return displayName();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public Collection<String> getGroups() {
        return groups();
    }
}
