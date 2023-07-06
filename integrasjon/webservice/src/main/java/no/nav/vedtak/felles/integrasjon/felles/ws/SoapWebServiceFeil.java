package no.nav.vedtak.felles.integrasjon.felles.ws;

import javax.security.auth.login.LoginException;
import jakarta.xml.ws.WebServiceException;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;

class SoapWebServiceFeil {

    private SoapWebServiceFeil() {

    }

    static IntegrasjonException soapFaultIwebserviceKall(String webservice, WebServiceException e) {
        return new IntegrasjonException("F-942048", String.format("SOAP tjenesten [ %s ] returnerte en SOAP Fault:", webservice), e);
    }

    static TekniskException feiletUtlogging(LoginException e) {
        return new TekniskException("F-668217", "Feilet utlogging.", e);
    }

    static IntegrasjonException midlertidigFeil(String webservice, WebServiceException e) {
        return new IntegrasjonException("F-134134", String.format("SOAP tjenesten [ %s ] returnerte en feil som trolig er midlertidig", webservice),
            e);
    }
}
