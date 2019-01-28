package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.InfotrygdBeregningsgrunnlagV1;
import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.meldinger.FinnGrunnlagListeRequest;
import no.nav.vedtak.exception.IntegrasjonException;

public class InfotrygdBeregningsgrunnlagConsumerTest {

    private InfotrygdBeregningsgrunnlagConsumer consumer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private InfotrygdBeregningsgrunnlagV1 mockWebservice = mock(InfotrygdBeregningsgrunnlagV1.class);

    @Before
    public void setUp() {
        consumer = new InfotrygdBeregningsgrunnlagConsumerImpl(mockWebservice);
    }

    @Test
    public void skalKasteIntegrasjonsfeilNårWebserviceSenderSoapFault() throws Exception {
        when(mockWebservice.finnGrunnlagListe(any(FinnGrunnlagListeRequest.class))).thenThrow(opprettSOAPFaultException("feil"));

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnBeregningsgrunnlagListe(mock(FinnGrunnlagListeRequest.class));
    }

    private SOAPFaultException opprettSOAPFaultException(String faultString) throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString(faultString);
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }

}
