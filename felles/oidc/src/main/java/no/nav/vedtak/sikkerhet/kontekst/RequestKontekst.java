package no.nav.vedtak.sikkerhet.kontekst;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public final class RequestKontekst extends BasisKontekst {

    private final OpenIDToken token;
    private UUID oid;
    private final Set<AnsattGruppe> grupper;

    private RequestKontekst(String uid, String kompaktUid, IdentType identType, String consumerId, OpenIDToken token, UUID oid, Set<AnsattGruppe> grupper) {
        super(SikkerhetContext.REQUEST, uid, kompaktUid, identType, consumerId);
        this.token = token;
        this.oid = oid;
        this.grupper = new HashSet<>(grupper);
    }

    public static RequestKontekst forRequest(String uid, String kompaktUid, IdentType identType, OpenIDToken token, UUID oid, Set<AnsattGruppe> grupper) {
        var konsumentId = Optional.ofNullable(MDCOperations.getConsumerId()).orElse(uid);
        return new RequestKontekst(uid, kompaktUid, identType, konsumentId, token, oid, grupper);
    }

    public OpenIDToken getToken() {
        return token;
    }

    public UUID getOid() {
        return oid;
    }

    public Set<AnsattGruppe> getGrupper() {
        return grupper;
    }

    public boolean harGruppe(AnsattGruppe gruppe) {
        return grupper.contains(gruppe);
    }
}
