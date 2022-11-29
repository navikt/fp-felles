package no.nav.vedtak.felles.integrasjon.felles.ws;

import no.nav.vedtak.log.mdc.MDCOperations;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.xml.bind.JAXBException;
import java.util.List;

public class CallIdOutInterceptor extends AbstractPhaseInterceptor<Message> {

    public CallIdOutInterceptor() {
        super(Phase.PRE_STREAM);
    }

    @Override
    public void handleMessage(Message message) {

        String callId = MDCOperations.getCallId();
        if (callId == null) {
            throw new IllegalStateException("CallId skal være tilgjengelig i MDC på dette tidspunkt.");
        }

        SoapMessage soapMessage;
        if (message instanceof SoapMessage) {
            soapMessage = (SoapMessage) message;
        } else {
            throw new IllegalStateException("message har uventet type");
        }
        List<Header> list = soapMessage.getHeaders();

        try {
            SoapHeader header = new SoapHeader(MDCOperations.CALLID_QNAME, callId, new JAXBDataBinding(String.class));
            list.add(header);
        } catch (JAXBException e) {
            throw new IllegalStateException("", e);
        }
    }
}
