package no.nav.vedtak.felles.integrasjon.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import no.nav.vedtak.feil.FeilFactory;

public class LdapBrukeroppslag {

    private final LdapContext context;
    private final LdapName searchBase;

    private static final Pattern IDENT_PATTERN = Pattern.compile("^\\p{LD}+$");

    public LdapBrukeroppslag() {
        context = LdapInnlogging.lagLdapContext();
        searchBase = lagLdapSearchBase();
    }

    LdapBrukeroppslag(LdapContext context, LdapName searcBase) {
        this.context = context;
        this.searchBase = searcBase;
    }

    public LdapBruker hentBrukerinformasjon(String ident) {
        SearchResult result = ldapSearch(ident);
        String displayName = getDisplayName(result);
        Collection<String> groups = getMemberOf(result);
        return new LdapBruker(displayName, groups);
    }

    private SearchResult ldapSearch(String ident) {
        if (ident == null || ident.isEmpty()) {
            throw FeilFactory.create(LdapFeil.class).kanIkkeSlåOppBrukernavnDaIdentIkkeErSatt().toException();
        }
        Matcher matcher = IDENT_PATTERN.matcher(ident);
        if (!matcher.matches()) {
            throw LdapFeil.FACTORY.ugyldigIdent(ident).toException();
        }

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setCountLimit(1);
        String søkestreng = String.format("(cn=%s)", ident);
        try {
            NamingEnumeration<SearchResult> result = context.search(searchBase, søkestreng, controls); // NOSONAR
            if (result.hasMoreElements()) {
                return result.nextElement();
            }
            throw LdapFeil.FACTORY.fantIngenBrukerForIdent(ident).toException();
        } catch (LimitExceededException lee) {
            throw LdapFeil.FACTORY.ikkeEntydigResultat(ident, lee).toException();
        } catch (NamingException e) {
            throw LdapFeil.FACTORY.ukjentFeilVedLdapSøk(søkestreng, e).toException();
        }
    }

    protected String getDisplayName(SearchResult result) {
        String attributeName = "displayName";
        Attribute displayName = find(result, attributeName);
        try {
            return displayName.get().toString();
        } catch (NamingException e) {
            throw LdapFeil.FACTORY.kunneIkkeHenteUtAttributtverdi(attributeName, displayName, e).toException();
        }
    }

    /**
     * <BLOCKQUOTE>
     * <p>The memberOf attribute is a multi-valued attribute that contains groups of which the user is a direct member, except for the primary group, which is represented by the primaryGroupId. Group membership is dependent on the domain controller (DC) from which this attribute is retrieved:</p>
     * <ul>
     * <li>At a DC for the domain that contains the user, memberOf for the user is complete with respect to membership for groups in that domain; however, memberOf does not contain the user's membership in domain local and global groups in other domains.</li>
     * <li>•At a GC server, memberOf for the user is complete with respect to all universal group memberships.</li>
     * </ul>
     * <p>
     * If both conditions are true for the DC, both sets of data are contained in memberOf.</p>
     * <p>
     * Be aware that this attribute lists the groups that contain the user in their member attribute—it does not contain the recursive list of nested predecessors. For example, if user O is a member of group C and group B and group B were nested in group A, the memberOf attribute of user O would list group C and group B, but not group A.</p>
     * <p>
     * This attribute is not stored—it is a computed back-link attribute</p>
     * </BLOCKQUOTE>
     * Source: <a href="https://msdn.microsoft.com/en-us/library/ms677943.aspx">MSDN > ... > Using Active Directory Domain Services > Managing Users > User Object Attributes</a>
     * <p></p>
     * <p>
     * OBS! Nøstede grupper vil <strong>ikke</strong> ligge i memberOf</p>
     *
     * @return CN-value av alle grupper brukere er <strong>direkte</strong> medlem av
     */
    protected Collection<String> getMemberOf(SearchResult result) {
        String attributeName = "memberOf";
        List<String> groups = new ArrayList<>();

        Attribute memberOf = find(result, attributeName);
        try {
            NamingEnumeration<?> all = memberOf.getAll();
            while (all.hasMoreElements()) {
                Object group = all.nextElement();
                String dnValue = group.toString();
                groups.add(dnValue);
            }
        } catch (NamingException e) {
            throw LdapFeil.FACTORY.kunneIkkeHenteUtAttributtverdi(attributeName, memberOf, e).toException();
        }
        return groups;
    }

    private Attribute find(SearchResult element, String attributeName) {
        Attribute attribute = element.getAttributes().get(attributeName);
        if (attribute == null) {
            throw LdapFeil.FACTORY.resultatFraLdapMangletAttributt(attributeName).toException();
        }
        return attribute;
    }

    private LdapName lagLdapSearchBase() {
        String userBaseDn = LdapInnlogging.getRequiredProperty("ldap.user.basedn");
        try {
            return new LdapName(userBaseDn); // NOSONAR
        } catch (InvalidNameException e) {
            throw LdapFeil.FACTORY.klarteIkkeDefinereBaseSøk(userBaseDn, e).toException();
        }
    }
}