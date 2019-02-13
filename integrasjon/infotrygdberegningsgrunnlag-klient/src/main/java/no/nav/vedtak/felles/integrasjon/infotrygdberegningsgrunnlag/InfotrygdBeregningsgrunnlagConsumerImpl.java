package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.FinnGrunnlagListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.InfotrygdBeregningsgrunnlagV1;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

public class InfotrygdBeregningsgrunnlagConsumerImpl implements InfotrygdBeregningsgrunnlagConsumer {
    public static final String SERVICE_IDENTIFIER = "InfotrygdBeregningsgrunnlagV1";

    private InfotrygdBeregningsgrunnlagV1 port;

    public InfotrygdBeregningsgrunnlagConsumerImpl(InfotrygdBeregningsgrunnlagV1 port) {
        this.port = port;
    }

    @Override
    public FinnGrunnlagListeResponse finnBeregningsgrunnlagListe(FinnGrunnlagListeRequest finnGrunnlagListeRequest) throws FinnGrunnlagListeSikkerhetsbegrensning, FinnGrunnlagListeUgyldigInput, FinnGrunnlagListePersonIkkeFunnet {
        try {
            return port.finnGrunnlagListe(finnGrunnlagListeRequest);
        } catch (SOAPFaultException e) { // NOSONAR
            throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
        }
    }
}