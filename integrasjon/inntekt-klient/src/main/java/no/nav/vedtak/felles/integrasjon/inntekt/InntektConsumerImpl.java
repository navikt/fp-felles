package no.nav.vedtak.felles.integrasjon.inntekt;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.inntekt.v3.binding.HentInntektListeBolkHarIkkeTilgangTilOensketAInntektsfilter;
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.HentInntektListeBolkUgyldigInput;
import no.nav.tjeneste.virksomhet.inntekt.v3.binding.InntektV3;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkRequest;
import no.nav.tjeneste.virksomhet.inntekt.v3.meldinger.HentInntektListeBolkResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class InntektConsumerImpl implements InntektConsumer {
    public static final String SERVICE_IDENTIFIER = "InntektV3";

    private InntektV3 port;

    public InntektConsumerImpl(InntektV3 port) {
        this.port = port;
    }

    @Override
    public HentInntektListeBolkResponse hentInntektListeBolk(HentInntektListeBolkRequest request)
            throws HentInntektListeBolkHarIkkeTilgangTilOensketAInntektsfilter, HentInntektListeBolkUgyldigInput {
        try {
            return port.hentInntektListeBolk(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
