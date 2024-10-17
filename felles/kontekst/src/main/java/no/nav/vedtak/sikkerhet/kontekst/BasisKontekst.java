package no.nav.vedtak.sikkerhet.kontekst;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;

public class BasisKontekst implements Kontekst {

    private final SikkerhetContext context;
    private final String uid;
    private final String kompaktUid;
    private final IdentType identType;
    private final String konsumentId;


    protected BasisKontekst(SikkerhetContext context, String uid, IdentType identType, String consumerId) {
        this(context, uid, uid, identType, consumerId);
    }

    protected BasisKontekst(SikkerhetContext context, String uid, String kompaktUid, IdentType identType, String consumerId) {
        this.context = context;
        this.uid = uid;
        this.kompaktUid = kompaktUid;
        this.identType = identType;
        this.konsumentId = consumerId;
    }

    @Override
    public SikkerhetContext getContext() {
        return context;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getKompaktUid() {
        return kompaktUid;
    }

    @Override
    public IdentType getIdentType() {
        return identType;
    }

    @Override
    public String getKonsumentId() {
        return konsumentId;
    }

    // Denne brukes i prosesstask
    public static BasisKontekst forProsesstaskUtenSystembruker() {
        var username = "srv" + Optional.ofNullable(Environment.current().application()).orElse("local");
        var konsument = Optional.ofNullable(Environment.current().clientId()).orElse(username);
        return new BasisKontekst(SikkerhetContext.SYSTEM, username, IdentType.Prosess, konsument);
    }

    public static BasisKontekst ikkeAutentisertRequest(String consumerId) {
        var consumer = Optional.ofNullable(consumerId)
            .or(() -> Optional.ofNullable(Environment.current().application()).map(a -> "srv" + a))
            .orElse("srvlocal");
        return new BasisKontekst(SikkerhetContext.REQUEST, null, null, consumer);
    }

    static BasisKontekst tomKontekst() {
        return new BasisKontekst(null, null, null, null);
    }

}
