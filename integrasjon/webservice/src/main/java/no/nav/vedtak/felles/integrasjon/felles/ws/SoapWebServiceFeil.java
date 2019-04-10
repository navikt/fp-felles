package no.nav.vedtak.felles.integrasjon.felles.ws;

import javax.security.auth.login.LoginException;
import javax.xml.ws.WebServiceException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface SoapWebServiceFeil extends DeklarerteFeil {
    SoapWebServiceFeil FACTORY = FeilFactory.create(SoapWebServiceFeil.class);

    @IntegrasjonFeil(feilkode = "FP-942048", feilmelding = "SOAP tjenesten [ %s ] returnerte en SOAP Fault: %s", logLevel = LogLevel.WARN)
    Feil soapFaultIwebserviceKall(String webservice, WebServiceException soapException);

    @TekniskFeil(feilkode = "F-668217", feilmelding = "Feilet utlogging.", logLevel = LogLevel.WARN)
    Feil feiletUtlogging(LoginException e);
}
