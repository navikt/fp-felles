package no.nav.vedtak.felles.integrasjon.person;


import java.net.SocketException;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class PersonConsumerImpl implements PersonConsumer {
    public static final String SERVICE_IDENTIFIER = "PersonV3";

    private PersonV3 port;

    public PersonConsumerImpl(PersonV3 port) {
        this.port = port;
    }

    @Override
    public HentPersonResponse hentPersonResponse(HentPersonRequest request) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        try {
            return port.hentPerson(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        } catch (WebServiceException e) {
            throw mapWebServiceException(e);
        }
    }

    @Override
    public HentPersonhistorikkResponse hentPersonhistorikkResponse(HentPersonhistorikkRequest request) throws HentPersonhistorikkSikkerhetsbegrensning, HentPersonhistorikkPersonIkkeFunnet {
        try {
            return port.hentPersonhistorikk(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        } catch (WebServiceException e) {
            throw mapWebServiceException(e);
        }
    }

    @Override
    public HentGeografiskTilknytningResponse hentGeografiskTilknytning(HentGeografiskTilknytningRequest request) throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {
        try {
            return port.hentGeografiskTilknytning(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        } catch (WebServiceException e) {
            throw mapWebServiceException(e);
        }
    }
    

    private static final RuntimeException mapWebServiceException(WebServiceException e) {
        if (isConnectionReset(e)) {
            return SoapWebServiceFeil.FACTORY.midlertidigFeil("TPS", e).toException();
        } else {
            return e;
        }
    }
    
    private static final boolean isConnectionReset(Throwable e) {
        if (e == null) {
            return false;
        }
        if (e instanceof SocketException
                && ("Connection reset".equals(e.getMessage())
                    || "Unexpected end of file from server".equals(e.getMessage()))) {
            return true;
        }
        return isConnectionReset(e.getCause());
    }
}
