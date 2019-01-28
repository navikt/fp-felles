
/**
 * Denne pakken inneholder felles Exception og {@link no.nav.vedtak.feil.Feil} presentasjon for å håndtere feilmeldinger
 * og feilkoder som logges.
 * 
 * Feilkoder som logges er unike og stabile,og kan benyttes i dokumentasjon (f.eks. i forbindelse med troubleshooting av
 * systemet).
 * 
 * Exceptions som applikasjonen selv kaster er subklasser av en av 3 typer:
 * <ul>
 * <li>FunksjonellException - Denne presenteres bruker, men logges normalt ikke</li>
 * <li>TekniskException - logges alltid, som WARN eller ERROR. Bruker får en feilmelding sammen med en CallId som kan
 * benyttes til å søke opp feilmelding i logger</li>
 * <li>IntegrasjonException - en spesiell TekniskException knyttet til kall mot andre tjenester</li>
 * </ul>
 */
package no.nav.vedtak;