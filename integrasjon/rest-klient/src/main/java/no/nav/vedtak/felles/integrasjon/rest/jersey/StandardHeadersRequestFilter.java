package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class StandardHeadersRequestFilter implements ClientRequestFilter {
    private static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    private static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    public static final String ALT_NAV_CALL_ID = "nav-call-id";

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        ctx.getHeaders().add(DEFAULT_NAV_CALLID, getCallId());
        ctx.getHeaders().add(ALT_NAV_CALL_ID, getCallId());
        ctx.getHeaders().add(DEFAULT_NAV_CONSUMERID, getConsumerId());
    }

    private static String getConsumerId() {
        return getSubjectHandler().getConsumerId();
    }
}
