package no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.binding.MeldekortUtbetalingsgrunnlagV1;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.meldekortutbetalingsgrunnlag.v1.meldinger.FinnMeldekortUtbetalingsgrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class MeldekortUtbetalingsgrunnlagConsumerImpl implements MeldekortUtbetalingsgrunnlagConsumer {
    public static final String SERVICE_IDENTIFIER = "MeldekortUtbetalingsgrunnlagV1";

    private MeldekortUtbetalingsgrunnlagV1 port;

    public MeldekortUtbetalingsgrunnlagConsumerImpl(MeldekortUtbetalingsgrunnlagV1 port) {
        this.port = port;
    }

    @Override
    public FinnMeldekortUtbetalingsgrunnlagListeResponse finnMeldekortUtbetalingsgrunnlagListe(FinnMeldekortUtbetalingsgrunnlagListeRequest request) throws FinnMeldekortUtbetalingsgrunnlagListeSikkerhetsbegrensning, FinnMeldekortUtbetalingsgrunnlagListeAktoerIkkeFunnet, FinnMeldekortUtbetalingsgrunnlagListeUgyldigInput {
        try {
            return port.finnMeldekortUtbetalingsgrunnlagListe(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}