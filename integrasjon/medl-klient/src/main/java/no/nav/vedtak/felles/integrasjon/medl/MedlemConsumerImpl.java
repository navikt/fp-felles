package no.nav.vedtak.felles.integrasjon.medl;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.medlemskap.v2.MedlemskapV2;
import no.nav.tjeneste.virksomhet.medlemskap.v2.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.medlemskap.v2.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeResponse;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class MedlemConsumerImpl implements MedlemConsumer {
    public static final String SERVICE_IDENTIFIER = "MedlemskapV2";

    private MedlemskapV2 port;

    public MedlemConsumerImpl(MedlemskapV2 port) {
        this.port = port;
    }

    @Override
    public HentPeriodeResponse hentPeriode(HentPeriodeRequest hentPeriodeRequest) throws Sikkerhetsbegrensning {
        try {
            return port.hentPeriode(hentPeriodeRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }

    @Override
    public HentPeriodeListeResponse hentPeriodeListe(HentPeriodeListeRequest hentPeriodeListeRequest) throws PersonIkkeFunnet, Sikkerhetsbegrensning {
        try {
            return port.hentPeriodeListe(hentPeriodeListeRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}
