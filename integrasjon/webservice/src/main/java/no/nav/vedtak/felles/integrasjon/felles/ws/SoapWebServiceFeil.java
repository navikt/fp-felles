package no.nav.vedtak.felles.integrasjon.felles.ws;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface SoapWebServiceFeil extends DeklarerteFeil {
    SoapWebServiceFeil FACTORY = FeilFactory.create(SoapWebServiceFeil.class);

    @IntegrasjonFeil(feilkode = "FP-942048", feilmelding = "SOAP tjenesten [ %s ] returnerte en SOAP Fault: %s", logLevel = LogLevel.WARN)
    Feil soapFaultIwebserviceKall(String webservice, SOAPFaultException soapException);
}
