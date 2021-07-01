package no.nav.vedtak.felles.integrasjon.ldap;

import javax.naming.LimitExceededException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;

class LdapFeil {

    private LdapFeil() {

    }

    static TekniskException kanIkkeSlåOppBrukernavnDaIdentIkkeErSatt() {
        return new TekniskException("F-344885", "Kan ikke slå opp brukernavn uten å ha ident");
    }

    static TekniskException ugyldigIdent(String ident) {
        return new TekniskException("F-271934", String.format("Mulig LDAP-injection forsøk. Søkte med ugyldig ident '%s'", ident));
    }

    static TekniskException klarteIkkeKobleTilLdap(String url, NamingException e) {
        return new TekniskException("F-222862", String.format("Klarte ikke koble til LDAP på URL %s", url));
    }

    static IntegrasjonException klarteIkkeDefinereBaseSøk(String baseSøk, NamingException e) {
        return new IntegrasjonException("F-703197", String.format("Kunne ikke definere base-søk mot LDAP %s", baseSøk), e);
    }

    static IntegrasjonException ukjentFeilVedLdapSøk(String søkestreng, NamingException e) {
        return new IntegrasjonException("F-690609", String.format("Uventet feil ved LDAP-søk %s", søkestreng), e);
    }

    static IntegrasjonException resultatFraLdapMangletAttributt(String attributtnavn) {
        return new IntegrasjonException("F-828846", String.format("Resultat fra LDAP manglet påkrevet attributtnavn %s", attributtnavn));
    }

    static TekniskException kunneIkkeHenteUtAttributtverdi(String attributtnavn, Attribute attribute, NamingException e) {
        return new TekniskException("F-314006", String.format("Kunne ikke hente ut attributtverdi %s fra %s", attributtnavn, attributtnavn), e);
    }

    static IntegrasjonException ikkeEntydigResultat(String ident, LimitExceededException e) {
        return new IntegrasjonException("F-137440",
                String.format("Forventet ett unikt resultat på søk mot LDAP etter ident %s, men fikk flere treff", ident), e);
    }

    static IntegrasjonException fantIngenBrukerForIdent(String ident) {
        return new IntegrasjonException("F-418891", String.format("Fikk ingen treff på søk mot LDAP etter ident %s", ident));
    }
}
