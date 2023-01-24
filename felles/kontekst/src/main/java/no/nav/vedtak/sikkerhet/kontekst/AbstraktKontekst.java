package no.nav.vedtak.sikkerhet.kontekst;

public abstract class AbstraktKontekst  {

    private final SikkerhetContext context;
    private final String uid;
    private final IdentType identType;
    private final String konsumentId;


    protected AbstraktKontekst(SikkerhetContext context, String uid, IdentType identType, String consumerId) {
        this.context = context;
        this.uid = uid;
        this.identType = identType;
        this.konsumentId = consumerId;
    }

    public boolean erSystemRessurs() {
        return SikkerhetContext.SYSTEM.equals(context) || identType.erSystem();
    }

    public boolean erLokalSystemRessurs() {
        return SikkerhetContext.SYSTEM.equals(context);
    }

    public SikkerhetContext getContext() {
        return context;
    }

    public String getUid() {
        return uid;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public String getKonsumentId() {
        return konsumentId;
    }

}
