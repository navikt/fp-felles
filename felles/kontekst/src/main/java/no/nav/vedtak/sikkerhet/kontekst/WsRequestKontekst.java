package no.nav.vedtak.sikkerhet.kontekst;

public final class WsRequestKontekst extends BasisKontekst {

    private final String samlTokenAsString;

    private WsRequestKontekst(String uid, IdentType identType, String consumerId, String samlTokenAsString) {
        super(SikkerhetContext.WSREQUEST, uid, identType, consumerId);
        this.samlTokenAsString = samlTokenAsString;
    }

    public static WsRequestKontekst forWsRequest(String uid, String consumerId, String samlTokenAsString) {
        return new WsRequestKontekst(uid, IdentType.InternBruker, consumerId, samlTokenAsString);
    }

    public String getSamlTokenAsString() {
        return samlTokenAsString;
    }
}
