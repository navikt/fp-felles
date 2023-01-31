package no.nav.vedtak.sikkerhet.kontekst;

public final class WsRequestKontekst extends AbstraktKontekst {

    private WsRequestKontekst(String uid, String consumerId) {
        super(SikkerhetContext.WSREQUEST, uid, IdentType.InternBruker, consumerId);
    }

    public static WsRequestKontekst forRequest(String uid, String consumerId) {
        return new WsRequestKontekst(uid, consumerId);
    }

}
