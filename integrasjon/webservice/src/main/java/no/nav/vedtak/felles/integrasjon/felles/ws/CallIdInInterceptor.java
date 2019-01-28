package no.nav.vedtak.felles.integrasjon.felles.ws;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

import no.nav.vedtak.log.mdc.MDCOperations;

public class CallIdInInterceptor extends AbstractSoapInterceptor {

    public CallIdInInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(SoapMessage message) {
        if (message.hasHeader(MDCOperations.CALLID_QNAME)) {
            Header header = message.getHeader(MDCOperations.CALLID_QNAME);
            MDCOperations.putCallId(((Element) header.getObject()).getTextContent());
        }
    }
}
