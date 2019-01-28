package no.nav.vedtak.felles.integrasjon.ldap;

import javax.naming.LimitExceededException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface LdapFeil extends DeklarerteFeil {

    LdapFeil FACTORY = FeilFactory.create(LdapFeil.class); // NOSONAR ok med konstant i interface her

    @TekniskFeil(feilkode = "F-344885", feilmelding = "Kan ikke slå opp brukernavn uten å ha ident", logLevel = LogLevel.WARN)
    Feil kanIkkeSlåOppBrukernavnDaIdentIkkeErSatt();

    @TekniskFeil(feilkode = "F-271934", feilmelding = "Mulig LDAP-injection forsøk. Søkte med ugyldig ident '%s'", logLevel = LogLevel.WARN)
    Feil ugyldigIdent(String ident);

    @IntegrasjonFeil(feilkode = "F-222862", feilmelding = "Klarte ikke koble til LDAP på URL %s", logLevel = LogLevel.WARN)
    Feil klarteIkkeKobleTilLdap(String url, NamingException e);

    @IntegrasjonFeil(feilkode = "F-703197", feilmelding = "Kunne ikke definere base-søk mot LDAP %s", logLevel = LogLevel.WARN)
    Feil klarteIkkeDefinereBaseSøk(String baseSøk, NamingException e);

    @TekniskFeil(feilkode = "F-055498", feilmelding = "Klarte ikke koble til LDAP da påkrevd system property (%s) ikke er satt", logLevel = LogLevel.WARN)
    Feil manglerLdapKonfigurasjon(String navn);

    @IntegrasjonFeil(feilkode = "F-690609", feilmelding = "Uventet feil ved LDAP-søk %s", logLevel = LogLevel.WARN)
    Feil ukjentFeilVedLdapSøk(String søkestreng, NamingException e);

    @IntegrasjonFeil(feilkode = "F-828846", feilmelding = "Resultat fra LDAP manglet påkrevet attributtnavn %s", logLevel = LogLevel.WARN)
    Feil resultatFraLdapMangletAttributt(String attributtnavn);

    @TekniskFeil(feilkode = "F-314006", feilmelding = "Kunne ikke hente ut attributtverdi %s fra %s", logLevel = LogLevel.WARN)
    Feil kunneIkkeHenteUtAttributtverdi(String attributtnavn, Attribute attribute, NamingException e);

    @IntegrasjonFeil(feilkode = "F-137440", feilmelding = "Forventet ett unikt resultat på søk mot LDAP etter ident %s, men fikk flere treff", logLevel = LogLevel.WARN)
    Feil ikkeEntydigResultat(String ident, LimitExceededException e);

    @IntegrasjonFeil(feilkode = "F-418891", feilmelding = "Fikk ingen treff på søk mot LDAP etter ident %s", logLevel = LogLevel.WARN)
    Feil fantIngenBrukerForIdent(String ident);
}
