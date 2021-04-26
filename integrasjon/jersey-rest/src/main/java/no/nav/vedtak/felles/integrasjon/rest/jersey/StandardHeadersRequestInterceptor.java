package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.ALT_NAV_CALL_ID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.HEADER_CORRELATION_ID;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

class StandardHeadersRequestInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest req, HttpContext context) throws HttpException, IOException {
        req.addHeader(DEFAULT_NAV_CALLID, getCallId());
        req.addHeader(ALT_NAV_CALL_ID, getCallId());
        req.addHeader(HEADER_CORRELATION_ID, getCallId());
        req.addHeader(DEFAULT_NAV_CONSUMERID, getSubjectHandler().getConsumerId());
    }
}
