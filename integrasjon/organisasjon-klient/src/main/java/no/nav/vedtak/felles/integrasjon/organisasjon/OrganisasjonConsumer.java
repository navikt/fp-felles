package no.nav.vedtak.felles.integrasjon.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonForJuridiskRequest;
import no.nav.vedtak.felles.integrasjon.organisasjon.hent.HentOrganisasjonRequest;

public interface OrganisasjonConsumer {

    /**
     * HentOrganisasjon kan brukes til å hente ut informasjon om en organisasjon med et gitt organisasjonsnummer.
     * Organisasjoner kan være juridiske enheter, orgledd og virksomheter.
     *
     * @param request 'skal innholde organisasjonsnummer'
     * @return HentOrganisasjonResponse
     * @throws HentOrganisasjonOrganisasjonIkkeFunnet 'OrganisasjonIkkeFunnet'
     * @throws HentOrganisasjonUgyldigInput           'OrganisasjonUgyldigInput'
     */
    HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput;

    /**
     * Skal hente ut den unike virksomhetens organisasjonsnummer som ligger under juridisk enhet med et gitt organisasjonsnummer.
     *
     * @param request 'skal innholde organisasjonsnummer'
     * @return HentOrganisasjonResponse
     */
    HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentOrganisajonerForJuridiskOrgnr(HentOrganisasjonForJuridiskRequest request);

}
