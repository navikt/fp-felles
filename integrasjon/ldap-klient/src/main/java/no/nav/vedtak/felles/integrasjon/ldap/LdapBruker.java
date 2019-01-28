package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.Collection;

public class LdapBruker {
    private final String displayName;
    private final Collection<String> groups;

    public LdapBruker(String displayName, Collection<String> groups) {
        this.displayName = displayName;
        this.groups = groups;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Collection<String> getGroups() {
        return groups;
    }
}
