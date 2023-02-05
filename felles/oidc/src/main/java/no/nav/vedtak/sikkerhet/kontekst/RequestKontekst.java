package no.nav.vedtak.sikkerhet.kontekst;

import java.util.Optional;

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public final class RequestKontekst extends BasisKontekst {

    private final OpenIDToken token;

    private RequestKontekst(String uid, IdentType identType, String consumerId, OpenIDToken token) {
        super(SikkerhetContext.REQUEST, uid, identType, consumerId);
        this.token = token;
    }

    public static RequestKontekst forRequest(String uid, IdentType identType, String consumerId, OpenIDToken token) {
        return new RequestKontekst(uid, identType, consumerId, token);
    }

    public static RequestKontekst forRequest(String uid, IdentType identType, OpenIDToken token) {
        var konsumentId = Optional.ofNullable(MDCOperations.getConsumerId()).orElse(uid);
        return new RequestKontekst(uid, identType, konsumentId, token);
    }

    public OpenIDToken getToken() {
        return token;
    }
}
