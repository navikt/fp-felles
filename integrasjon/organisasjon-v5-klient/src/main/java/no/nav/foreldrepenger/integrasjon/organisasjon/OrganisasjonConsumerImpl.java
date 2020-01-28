package no.nav.foreldrepenger.integrasjon.organisasjon;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.foreldrepenger.integrasjon.organisasjon.hent.HentOrganisasjonForJuridiskRequest;
import no.nav.foreldrepenger.integrasjon.organisasjon.hent.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5;
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.Organisasjonsfilter;
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class OrganisasjonConsumerImpl implements OrganisasjonConsumer {
    public static final String SERVICE_IDENTIFIER = "OrganisasjonV5";

    private OrganisasjonV5 port;

    public OrganisasjonConsumerImpl(OrganisasjonV5 port) {
        this.port = port;
    }

    @Override
    public HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request) throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        try {
            return port.hentOrganisasjon(convertToWSRequest(request));
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentOrganisajonerForJuridiskOrgnr(HentOrganisasjonForJuridiskRequest request) {
        try {
            return port.hentVirksomhetsOrgnrForJuridiskOrgnrBolk(convertToWSRequestJuridisk(request));
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    private no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest convertToWSRequest(HentOrganisasjonRequest request) {
        no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest wsRequest = new no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonRequest();
        wsRequest.setInkluderAnsatte(request.getMedAntallAnsatte());
        wsRequest.setOrgnummer(request.getOrgnummer());
        return wsRequest;
    }

    private no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest convertToWSRequestJuridisk(HentOrganisasjonForJuridiskRequest request) {
        HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest result = new HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest();
        Organisasjonsfilter organisasjonsfilter = new Organisasjonsfilter();
        organisasjonsfilter.setOrganisasjonsnummer(request.getOrgnummer());
        organisasjonsfilter.setHentingsdato(DateUtil.convertToXMLGregorianCalendar(request.getHenteForDato()));
        result.getOrganisasjonsfilterListe().add(organisasjonsfilter);
        return result;
    }
}
