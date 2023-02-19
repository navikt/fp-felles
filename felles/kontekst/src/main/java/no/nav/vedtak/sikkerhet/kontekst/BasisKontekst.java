package no.nav.vedtak.sikkerhet.kontekst;

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

    public static BasisKontekst forProsesstask() {
        return new BasisKontekst(SikkerhetContext.SYSTEM, Systembruker.username(), IdentType.Prosess, Systembruker.username());
    }

    public static BasisKontekst ikkeAutentisertRequest(String consumerId) {
        return new BasisKontekst(SikkerhetContext.REQUEST, null, null, ensureCunsumerId(consumerId));
    }

    static BasisKontekst tomKontekst() {
        return new BasisKontekst(null, null, null, null);
    }

    protected static String ensureCunsumerId(String consumerId) {
        return consumerId != null ? consumerId : Systembruker.username();
    }

}
