package no.nav.vedtak.sikkerhet.kontekst;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;

public class BasisKontekst implements Kontekst {

    private static final String APP_PSEUDO_USERID = "srv" + Optional.ofNullable(Environment.current().application()).orElse("local");
    private static final String APP_CLIENTID = Optional.ofNullable(Environment.current().clientId()).orElse(APP_PSEUDO_USERID);

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
        return new BasisKontekst(SikkerhetContext.SYSTEM, APP_PSEUDO_USERID, IdentType.Prosess, APP_CLIENTID);
    }

    public static BasisKontekst ikkeAutentisertRequest(String consumerId) {
        var consumer = Optional.ofNullable(consumerId).orElse(APP_PSEUDO_USERID);
        return new BasisKontekst(SikkerhetContext.REQUEST, null, null, consumer);
    }

    public static String getAppConsumerId() {
        return APP_CLIENTID;
    }

    static BasisKontekst tomKontekst() {
        return new BasisKontekst(null, null, null, null);
    }

}
