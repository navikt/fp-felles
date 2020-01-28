package no.nav.foreldrepenger.integrasjon.organisasjon;


import no.nav.foreldrepenger.integrasjon.organisasjon.hent.HentOrganisasjonForJuridiskRequest;
import no.nav.foreldrepenger.integrasjon.organisasjon.hent.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse;

public interface OrganisasjonConsumer {

    /** HentOrganisasjon kan brukes til å hente ut informasjon om en organisasjon med et gitt organisasjonsnummer.
     * Organisasjoner kan være juridiske enheter, orgledd og virksomheter.
     * @param request 'skal innholde organisasjonsnummer'
     * @return HentOrganisasjonResponse
     * @throws HentOrganisasjonOrganisasjonIkkeFunnet 'OrganisasjonIkkeFunnet'
     * @throws HentOrganisasjonUgyldigInput 'OrganisasjonUgyldigInput'
     */
    HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput;

    /**Skal hente ut den unike virksomhetens organisasjonsnummer som ligger under juridisk enhet med et gitt organisasjonsnummer.
     *
     * @param request 'skal innholde organisasjonsnummer'
     * @return HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse
     */
    HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentOrganisajonerForJuridiskOrgnr(HentOrganisasjonForJuridiskRequest request);
}
