package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.ObjectFactory;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkRequest;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class ArbeidsforholdConsumerTest {

    private static final String FNR = "03108940181";
    private static final String REGELVERK = "ALLE";
    private ObjectFactory objectFactory = new ObjectFactory();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    ArbeidsforholdConsumer consumer;
    ArbeidsforholdV3 mockArbeidsforholdV3 = mock(ArbeidsforholdV3.class);

    @Before
    public void setUp() {
        consumer = new ArbeidsforholdConsumerImpl(mockArbeidsforholdV3);
    }

    @Test
    public void skalFangeSoapFaulOgKasteFeilmelding() throws Exception {

        FinnArbeidsforholdPrArbeidstakerRequest request = opprettRequest();

        when(mockArbeidsforholdV3.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class))).thenThrow(opprettSOAPFaultException());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.finnArbeidsforholdPrArbeidstaker(request);
    }

    @Test
    public void skalFangeSoapFaul2OgKasteFeilmelding() throws Exception {

        HentArbeidsforholdHistorikkRequest request = new HentArbeidsforholdHistorikkRequest();
        request.setArbeidsforholdId(1L);

        when(mockArbeidsforholdV3.hentArbeidsforholdHistorikk(any(HentArbeidsforholdHistorikkRequest.class))).thenThrow(opprettSOAPFaultException());

        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("FP-942048");

        consumer.hentArbeidsforholdHistorikk(request);
    }

    private FinnArbeidsforholdPrArbeidstakerRequest opprettRequest() throws Exception {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();

        NorskIdent norskIdent = objectFactory.createNorskIdent();
        norskIdent.setIdent(FNR);
        request.setIdent(norskIdent);

        Periode arbeidsforholdIPeriode = objectFactory.createPeriode();
        arbeidsforholdIPeriode.setFom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2016, 5, 1)));
        arbeidsforholdIPeriode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(2017, 10, 31)));
        request.setArbeidsforholdIPeriode(arbeidsforholdIPeriode);

        Regelverker rapportertSomRegelverk = objectFactory.createRegelverker();
        rapportertSomRegelverk.setValue(REGELVERK);
        request.setRapportertSomRegelverk(rapportertSomRegelverk);

        return request;
    }

    private SOAPFaultException opprettSOAPFaultException() throws SOAPException {
        SOAPFault fault = SOAPFactory.newInstance().createFault();
        fault.setFaultString("testing");
        fault.setFaultCode(new QName("local"));
        return new SOAPFaultException(fault);
    }
}